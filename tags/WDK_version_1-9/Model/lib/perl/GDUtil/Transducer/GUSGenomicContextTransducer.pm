#!/usr/bin/perl

#------------------------------------------------------------------------
# GUSGenomicContextTransducer.pm
#
# Generate an image of the region surrounding a gene.  The code herein
# was basically stolen from GUSGenomicSeqTransducer, so some refactoring
# is obviously required.  It's also still completely PlasmoDB-specific.
#
# Jonathan Crabtree
#
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::Transducer::GUSGenomicContextTransducer;

use strict;

use DBI;

use WDK::Model::GDUtil::GDCanvas;
use WDK::Model::GDUtil::Span;
use WDK::Model::GDUtil::StripeSpan;
use WDK::Model::GDUtil::HorizLineSpan;
use WDK::Model::GDUtil::VertLineSpan;
use WDK::Model::GDUtil::PercentATSpan;
use WDK::Model::GDUtil::AxisSpan;
use WDK::Model::GDUtil::Packer;

#-------------------------------------------------
# Defaults
#-------------------------------------------------

my $DFLT_IMG_WIDTH = 400;
my $DFLT_IMG_HEIGHT = 300;
my $DFLT_JAVASCRIPT = 1;
my $DFLT_AUTO_HEIGHT = 1;
my $DFLT_TICK_INTERVAL = 'ends';

#-------------------------------------------------
# GUSGenomicContextTransducer
#-------------------------------------------------

my $cp0 = &WDK::Model::GDUtil::Packer::constantPacker(0);
my $lrp1 = &WDK::Model::GDUtil::Packer::leftToRightPacker(2);

sub new {
    my($class, $args) = @_;

    my $iw = $args->{imgWidth};
    my $ih = $args->{imgHeight};
    my $js = $args->{javascript};
    my $autoheight = $args->{autoheight};
    my $tickInterval = $args->{tickInterval};

    my $self = {
	width => (defined $iw) ? $iw : $DFLT_IMG_WIDTH,
	height => (defined $ih) ? $ih : $DFLT_IMG_HEIGHT,
	javascript => (defined $js) ? $js : $DFLT_JAVASCRIPT,
	x1 => $args->{x1},
	x2 => $args->{x2},
	naSeqId => $args->{naSeqId},
	seqSrcId => $args->{seqSrcId},
	geneSrcId => $args->{geneSrcId},
	geneNaFeatId => $args->{geneNaFeatId},
	chromosome => $args->{chromosome},
	dbh => $args->{dbh},
	autoheight => (defined $autoheight) ? $autoheight : $DFLT_AUTO_HEIGHT,
	tickInterval => (defined $tickInterval) ? $tickInterval : $DFLT_TICK_INTERVAL,
	coreDb => $args->{coreDb},
	sresDb => $args->{sresDb},
	dotsDb => $args->{dotsDb},
    };
    
    bless $self, $class;

    $self->{geneHrefFn} = $args->{geneHrefFn} if (defined($args->{geneHrefFn}));
    $self->{geneHrefTarget} = $args->{geneHrefTarget} if (defined($args->{geneHrefTarget}));
    $self->{name} = $args->{name} if (defined($args->{name}));
    $self->{seqHref} = $args->{seqHref} if (defined($args->{seqHref}));
    $self->{seqLabel} = $args->{seqLabel} if (defined($args->{seqLabel}));
    $self->{hrefTarget} = $args->{hrefTarget} if (defined($args->{hrefTarget}));

    # To avoid calling getRootSpanAndCanvas more than once
    #
    $self->{rootSpanCache} = undef;
    return $self;
}

sub getHtml {
    my($self, $imgURL) = @_;
    
    my $w = $self->{width};
    my $h = $self->{height};
    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;

    my $html = '';
    $html .= $self->getImageMap();
    $html .= "<IMG SRC=\"${imgURL}\" BORDER=\"0\" WIDTH=\"$w\" HEIGHT=\"$h\" ";
    $html .= "USEMAP=\"#${name}\">";

    return $html;
}

sub getImageMap {
    my($self) = @_;
    my $name = $self->{name};

    my ($rs, $canvas) = $self->getRootSpanAndCanvas();

    my $im .= "<MAP NAME=\"$name\">\n";
    $im .= $rs->makeImageMap($self->{canvas});
    $im .= "</MAP>\n";

    return $im;
}

sub getPng {
    my($self) = @_;
    my ($rs, $canvas) = $self->getRootSpanAndCanvas();
    my $img = $self->{canvas}->getImage();
    return $img->can('png') ? $img->png() : $img->gif();
}

sub getJpeg {
    my($self, $quality) = @_;
    my ($rs, $canvas) = $self->getRootSpanAndCanvas();

    if (defined($quality)) {
	return $canvas->getImage()->jpeg($quality);
    }
    return $canvas->getImage()->jpeg();
}

# Put the image together
#
sub getRootSpanAndCanvas {
    my($self) = @_;

    my $cache = $self->{rootSpanCache};
    return @$cache if ($cache);

    my $x1 = $self->{x1};
    my $x2 = $self->{x2};
    my $chr = $self->{chromosome};
    my $mapName = $self->{name};
    
    my $canvas =  
      WDK::Model::GDUtil::GDCanvas->new($self->{width}, $self->{height},
			    {x1 => 150, x2 => $self->{width} - 25},
			    {x1 => $x1, x2 => $x2 });
    $canvas->allocateWebSafePalette();

    my $black = $canvas->getImage()->colorExact(0,0,0);
    my $grey = $canvas->getImage()->colorExact(204,204,204);
    my $lblue = $canvas->getImage()->colorExact(153,153,255);

    my $topLevelSpans = [];

    # Genes & gene predictions
    #
    my $gSpans = $self->getGenePredictionSpans($x1, $x2, $canvas);
    my @predTypes = keys %$gSpans;
    
    push(@predTypes, 'FullPhat') if (!grep(/FullPhat/, @predTypes));
    push(@predTypes, 'Genefinder') if (!grep(/Genefinder/, @predTypes));
    push(@predTypes, 'GlimmerM') if (!grep(/GlimmerM/, @predTypes));

    # There's a single track in PlasmoDB 4.0 for all the sequencing center predictions
    #
    push(@predTypes, 'Pf Annotation') if (!grep(/pf annotation/i, @predTypes));
    
    foreach my $alg (sort { $b cmp $a } @predTypes) {
	my $fwdSpans = $gSpans->{$alg}->{fwd};
	my $revSpans = $gSpans->{$alg}->{rev};

	push(@$topLevelSpans, $self->makeStripeSpan($revSpans, "$alg (-)"));
	push(@$topLevelSpans, WDK::Model::GDUtil::HorizLineSpan->new({height => 1, color => $grey}));
	push(@$topLevelSpans, $self->makeStripeSpan($fwdSpans, "$alg (+)"));
#	push(@$topLevelSpans, $self->makeSpacer());
    }

    # TO DO - move this between official and other predictions

    # EST BLAT alignments
    #
    my $estSpans = $self->getESTAlignmentSpans($x1, $x2, $canvas);
    push(@$topLevelSpans, $self->makeStripeSpan($estSpans->{rev}, "Pf EST/GSS (-)", $cp0));
    push(@$topLevelSpans, WDK::Model::GDUtil::HorizLineSpan->new({height => 1, color => $grey}));
    push(@$topLevelSpans, $self->makeStripeSpan($estSpans->{fwd}, "Pf EST/GSS (+)", $cp0));
    
    # Hexamer predictions stored in HexamerFeature
    #
    my $hs = $self->getHexamerSpans($x1, $x2, $canvas);
	
    push(@$topLevelSpans, $self->makeStripeSpan($hs->{rev}, "Hexamer (-)", $cp0));
    push(@$topLevelSpans, WDK::Model::GDUtil::HorizLineSpan->new({height => 1, color => $grey}));
    push(@$topLevelSpans, $self->makeStripeSpan($hs->{fwd}, "Hexamer (+)", $cp0));
    push(@$topLevelSpans, $self->makeSpacer());

    # Low complexity sequence
    #
    my $lcSpans = $self->getLowComplexitySpans($x1, $x2, $canvas);
    push(@$topLevelSpans, $self->makeStripeSpan($lcSpans, "low complexity seq.", $cp0));

    # Percent AT plot, window size = 100bp
    # Only display percent AT plot if sequence < 200kb ?
    #
    my $seqLen = $x2 - $x1;

    if ($seqLen <= 200000) {
	my $args = {
	    x1 => $x1, x2 => $x2, 
	    naseq => $self->getSequence(), 
	    height => 30, label => '%AT plot (W=100)', windowSize => 100,
	    imagemapLabel => "Percent-AT plot with window size = 100bp",
	    imagemapHref => '',
	    lineColor => $lblue,
	};
	
	if ($self->{javascript}) {
	    my $sh = &safeHtml("Percent-AT plot with window size = 100bp");
	    my $mOver = "show${mapName}Info('$sh', '$x1-$2', 'percent-AT plot', ''); return true;";
	    $args->{imagemapOnMouseOver} = $mOver;
	}
	
	my $at100 = WDK::Model::GDUtil::PercentATSpan->new($args);
	push(@$topLevelSpans, $at100);
    }
    push(@$topLevelSpans, $self->makeSpacer());

    # Centromeres and telomeres
    #
    my $cenTelSpans = $self->getCentromereTelomereSpans($x1, $x2, $canvas);
    push(@$topLevelSpans, $self->makeSpacer());
    push(@$topLevelSpans, $self->makeStripeSpan($cenTelSpans, "telomere/centromere"));

    # Leave a little extra space at the top of the image
    #
    push(@$topLevelSpans, $self->makeSpacer());

    # Add ScaffoldGapFeatureSpans last
    #
    my $sgfs = $self->getScaffoldGapFeatureSpans($x1, $x2, $canvas);
    push(@$topLevelSpans, @$sgfs);

    # Tick interval
    #
    my $ti = 50;
    $ti = 'ends' if ($self->{seqLen} == $x2);

    my $rootSpan = 
      WDK::Model::GDUtil::AxisSpan->new({
	  x1 => $x1, 
	  x2 => $x2,
	  y1 => $self->{height} - 5,
	  height => 6, tickHeight => 4, tickWidth => 1,
	  kids => $topLevelSpans,
	  packer => WDK::Model::GDUtil::Packer::simplePacker(2),
	  tickInterval => $ti,
	  tickLabel => 'bp',
	  label => $self->{seqSrcId},
	  labelVAlign => 'bottom'
      });
    
    $rootSpan->pack();
    
    # Determine correct height and repack
    #
    if ($self->{autoheight}) {
	$self->{height} = $rootSpan->getHeight() + 5;
	
	$canvas = 
	  WDK::Model::GDUtil::GDCanvas->new($self->{width}, $self->{height},
				{x1 => 150, x2 => $self->{width} - 25},
				{x1 => $x1, x2 => $x2});
	
	$canvas->allocateWebSafePalette();
	$self->{canvas} = $canvas;
	$rootSpan->{y1} = $self->{height} - 5;
	$rootSpan->pack();
    }
    
    $rootSpan->draw($canvas);
    my @result = ($rootSpan, $canvas);
    $self->{rootSpanCache} = \@result;
    return @result;
}

# Get the sequence for this region of the DNA
#
sub getSequence {
    my($self) = @_;
    my $dbh = $self->{dbh};
    my $x1 = $self->{x1};
    my $x2 = $self->{x2};
    my $naSeqId = $self->{naSeqId};
    my $len = $x2 - $x1 + 1;
    my $seq = '';
    
    my $query = "select DBMS_LOB.SUBSTR(sequence,?,?) from NASequenceImp where na_sequence_id = $naSeqId";
    my $stmt = $dbh->prepare($query);

    for(my $s = $x1; $s < $x1 + $len;$s += 4000){
	$stmt->execute($s + 4000 <= $x1 + $len ? 4000 : $len + $x1 - $s,$s);
	while(my($str) = $stmt->fetchrow_array()){
	    $seq .= $str;
	}
    }
    return $seq;
}

# Return all gene predictions that overlap a specified region.
#
sub getGenePredictionSpans {
    my($self, $start, $end, $canvas) = @_;

    my $coreDb = $self->{coreDb};
    my $sresDb = $self->{sresDb};
    my $dotsDb = $self->{dotsDb};

    my $img = $canvas->getImage();

    my $fgColor = $canvas->getDefaultFgColor();
    my $annotatedColor = $img->colorExact(255,153,153);
    my $dfltColor = $img->colorExact(100,100,150);

    # Render the automated gene predictions in a lighter shade of grey
    # to focus attention on the official annotations.
    #
    my $glimmerMColor = $img->colorExact(153,153,153);
    my $fullPhatColor = $img->colorExact(153,153,153);
    my $genefinderColor = $img->colorExact(153,153,153);

    my $highlightColor = $img->colorExact(255,0,0);

    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $hrefFn = $self->{geneHrefFn};
    my $hrefTarget = $self->{geneHrefTarget};
    my $mapName = $self->{name};

    # For "official" sequencing center/centre annotation
    #
    my $annotator = undef;
    my $chr = $self->{'chromosome'};

    my $geneRows = [];

    my $sql = ("select distinct gf.na_feature_id, gf.name, gf.source_id, gf.product, " .
	       "ed.name, a.name, gf.gene_type, nal.start_min, nal.end_max, " .
	       "nal.is_reversed, gf.number_of_exons " .
	       "from ${dotsDb}.GeneFeature gf, ${dotsDb}.NALocation nal, " .
	       "${sresDb}.ExternalDatabaseRelease edr, ${sresDb}.ExternalDatabase ed, ${coreDb}.Algorithm a " .
	       "where gf.na_sequence_id = $naSeqId " .
	       "and gf.na_feature_id = nal.na_feature_id " .
	       "and nal.start_min <= $end " .
	       "and nal.end_max >= $start " .
	       "and a.algorithm_id (+) = gf.prediction_algorithm_id " .
	       "and edr.external_database_release_id (+) = gf.external_database_release_id " .
	       "and edr.external_database_id = ed.external_database_id (+) " .
	       "order by nal.start_min, a.name ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while (my @row = $sth->fetchrow_array()) { push(@$geneRows, \@row); }
    $sth->finish();

    # Hashref indexed by GeneFeature.na_feature_id
    #
    my $geneExonHash = {};
    # Query for coordinates of all the exons of the above genes
    #
    my $exonSql = ("select distinct ef.order_number, el.start_min, el.start_max, " . 
                   "el.end_min, el.end_max, gf.na_feature_id ".
		   "from ${dotsDb}.GeneFeature gf, ${dotsDb}.NALocation gl, " .
		   "${dotsDb}.ExonFeature ef, ${dotsDb}.NALocation el " .
		   "where gf.na_sequence_id = $naSeqId " .
		   "and gf.na_feature_id = gl.na_feature_id " .
		   "and gl.start_min <= $end " .
		   "and gl.end_max >= $start " .
		   "and ef.parent_id = gf.na_feature_id " .
		   "and el.na_feature_id = ef.na_feature_id " .
		   "order by gf.na_feature_id, el.start_min asc ");

    my $sth = $dbh->prepare($exonSql);
    $sth->execute();

    while (my @row = $sth->fetchrow_array()) {
        if ($geneExonHash->{$row[5]}) {
            push (@{ $geneExonHash->{$row[5]} }, \@row);
        } else {
            $geneExonHash->{$row[5]} = [\@row];
        } 
    }
    $sth->finish();

    # Gene predictions indexed by prediction method (Algorithm.name)
    # and then by strand.
    #
    my $genePredSpans = {};

    my $genesFwd = [];
    my $genesRev = [];

    foreach my $row (@$geneRows) {
	my $naFeatId = $row->[0];
	my $name = $row->[1];
	my $sourceId = $row->[2];
	my $product = $row->[3];
	my $dbname = $row->[4];
	my $alg = $row->[5];
	my $type = $row->[6];
	my $startMin = $row->[7];
	my $endMax = $row->[8];
	my $isRev = $row->[9];
	my $numExons = $row->[10];

	$alg = 'unknown' if ($alg =~ /^\s*$/);

	my $exonHeight = 8;
	my $intronOffset = -3;

	if ($dbname =~ /Plasmodium_.*Sanger/i) {
	    $annotator = 'Sanger';
	} elsif ($dbname =~ /Plasmodium_.*TIGR/i) {
	    $annotator = 'TIGR';
	} elsif ($dbname =~ /Plasmodium_.*Stanford/i) {
	    $annotator = 'Stanford';
	} else {
	    $annotator = undef;
	}

	my $color;
	my $intronColor;
	my $exonBorder = 0;

	if ($naFeatId == $self->{geneNaFeatId}) {
	    $color = $highlightColor;
	    $intronColor = $fgColor;
	    $exonBorder = 1;
	} elsif (defined($annotator)) {
	    $color = $annotatedColor;
	    $intronColor = $fgColor;
	    $exonBorder = 1;
	} elsif ($alg =~ /glimmerm/i) {
	    $intronColor = $color = $glimmerMColor;
	} elsif ($alg =~ /fullphat/i) {
	    $intronColor = $color = $fullPhatColor;
	} elsif ($alg =~ /genefinder/i) {
	    $intronColor = $color = $genefinderColor;
	} else {
	    $intronColor = $color = $dfltColor;
	}

	if (defined($annotator)) {
	    $exonHeight = 10;
	    $intronOffset = -4;
	}

	my $exons = [];
	my $fcolor = "#000000";

	my $geneX1 = undef;
	my $geneX2 = undef;
	my $lastX1 = undef;
	my $lastX2 = undef;

        my $rows = $geneExonHash->{$naFeatId};

        foreach my $row (@$rows) {
	    my ($x1, $x2);
	    if ($row->[1] < $row->[4]) {
		$x1 = $row->[1];
		$x2 = $row->[4];
	    } else {
		$x1 = $row->[4];
		$x2 = $row->[1];
	    }

	    # Truncate/remove exons and introns at boundaries
	    #
	    if ($x2 < $start) {
		$lastX1 = $x1; $lastX2 = $x2;
		next;
	    }
	    last if ($x1 > $end);
	    $x1 = $start if ($x1 < $start);
	    $x2 = $end if ($x2 > $end);

	    # Draw introns
	    #
	    if (defined($lastX1)) {
		if ($lastX2 < $x1) {
		    $lastX2 = $start if ($lastX2 < $start);
		    push(@$exons, &makeIntron($lastX2, $x1, $intronColor, $intronOffset));
		} else {
		    push(@$exons, &makeIntron($x2, $lastX1, $intronColor, $intronOffset));
		}
	    }

	    push(@$exons, &makeExon($x1, $x2, $color, $exonBorder, $exonHeight));

	    if (not defined($geneX1)) {
		$geneX1 = $x1; $geneX2 = $x2;
	    } else {
		$geneX1 = $x1 if ($x1 < $geneX1);
		$geneX2 = $x2 if ($x2 > $geneX2);
	    }
	    $lastX1 = $x1; $lastX2 = $x2;
	}

	my $descr;

	if (defined($annotator)) {
	    $descr = "${sourceId}: ${product} [$type gene annotated by $annotator]";
	} else {
	    $descr = "${sourceId}: ${product} [$type gene predicted by $alg]";
	}
	$descr = &safeHtml($descr);

	my $gArgs = {
	    x1 => $geneX1,
	    x2 => $geneX2,
	    kids => $exons,
	    shape => 'none',
	    height => 0,
	    imagemapLabel => $descr,
	    packer => $cp0,
	};
		
	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$descr';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$descr';" .
		 "return true;");
	    $gArgs->{imagemapOnMouseOver} = $mOver;
	}

	if ($naFeatId == $self->{geneNaFeatId}) {
	    $gArgs->{imagemapHref} = "#";
	} else {
	    $gArgs->{imagemapHref} = &$hrefFn($sourceId) if (defined($hrefFn));
	    $gArgs->{imagemapTarget} = $hrefTarget if (defined($hrefTarget));
	}

	my $gene = WDK::Model::GDUtil::Span->new($gArgs);

	# Group by official annotator or gene prediction method
	#
	my $groupKey = $alg;
	my $algSpans = $genePredSpans->{$groupKey};
	my($fwd, $rev);

	if (!defined($algSpans)) {
	    $fwd = []; $rev = [];
	    $genePredSpans->{$groupKey} = {fwd => $fwd, rev => $rev};
	} else {
	    $fwd = $algSpans->{fwd};
	    $rev = $algSpans->{rev};
	}

	if ($isRev) {
	    push(@$rev, $gene);
	} else {
	    push(@$fwd, $gene);
	}
    }
    return $genePredSpans;
}

# Get ESTs aligned to genomic sequence using BLAT
#
sub getESTAlignmentSpans {
    my($self, $start, $end, $canvas) = @_;

    my $dotsDb = $self->{dotsDb};

    my $fwdSpans = [];
    my $revSpans = [];

    my $img = $canvas->getImage();
    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $mapName = $self->{name};

    my $img = $canvas->getImage();
    my $estColor = $img->colorExact(204,102,153);
    my $gssColor = $img->colorExact(153,102,204);

    my $exonHeight = 10;
    my $intronOffset = -4;

    my $sql = ("select ena.source_id, st.name, ena.length, ba.percent_identity, " .
	       "ba.is_reversed, ba.number_of_spans, ba.blocksizes, ba.qstarts, ba.tstarts " .
	       "from ${dotsDb}.BLATAlignment ba, ${dotsDb}.ExternalNASequence ena, ${dotsDb}.SequenceType st " .
	       "where ba.target_na_sequence_id = $naSeqId " .
	       "and ba.blat_alignment_quality_id = 1 " .
	       "and ba.query_na_sequence_id = ena.na_sequence_id " .
	       "and ba.target_start <= $end " .
	       "and ba.target_end >= $start " .
	       "and ena.sequence_type_id = st.sequence_type_id ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while(my($src, $type, $len, $pct, $isrev, $nb, $bs, $qs, $ts) = $sth->fetchrow_array()) {
	my @bSizes = split(',', $bs);
	my @tStarts = split(',', $ts);
	my $exons = [];

	my $geneX1 = undef;
	my $geneX2 = undef;
	my $lastX1 = undef;
	my $lastX2 = undef;

	my $color = ($type =~ /EST/i) ? $estColor : $gssColor;

	# Process blocks/exons + introns
	#
	for (my $i = 0;$i < $nb; ++$i) {
	    my $size = $bSizes[$i];
	    my $tstart = $tStarts[$i];

	    my $x1 = $tstart + 1;
	    my $x2 = $tstart + $size;

	    # Truncate exon to image boundaries
	    #
	    $x1 = $start if ($x1 < $start);
	    $x2 = $end if ($x2 > $end);

	    # Draw introns
	    #
	    if (defined($lastX1)) {
		if ($lastX2 < $x1) {
		    $lastX2 = $start if ($lastX2 < $start);
		    push(@$exons, &makeIntron($lastX2, $x1, $color, $intronOffset));
		} else {
		    push(@$exons, &makeIntron($x2, $lastX1, $color, $intronOffset));
		}
	    }

	    push(@$exons, &makeExon($x1, $x2, $color, 0, $exonHeight));

	    if (not defined($geneX1)) {
		$geneX1 = $x1; $geneX2 = $x2;
	    } else {
		$geneX1 = $x1 if ($x1 < $geneX1);
		$geneX2 = $x2 if ($x2 > $geneX2);
	    }
	    $lastX1 = $x1; $lastX2 = $x2;
	}

	my $descr = &safeHtml("$src: $len bp $type aligned by BLAT at $pct% identity with $nb span(s)");

	my $args = {
	    x1 => $geneX1,
	    x2 => $geneX2,
	    kids => $exons,
	    shape => 'none',
	    height => 0,
	    imagemapLabel => $descr,
	    packer => $cp0
	};
			
	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$descr';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$descr';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	# TO DO - provide link to detailed BLAT alignment

	my $align = WDK::Model::GDUtil::Span->new($args);

	if ($isrev) {
	    push(@$revSpans, $align);
	} else {
	    push(@$fwdSpans, $align);
	}
    }

    $sth->finish();
    return {fwd => $fwdSpans, rev => $revSpans};
}

sub getLowComplexitySpans {
    my($self, $start, $end, $canvas) = @_;

    my $coreDb = $self->{coreDb};
    my $dotsDb = $self->{dotsDb};

    my $spans = [];

    my $img = $canvas->getImage();
    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $mapName = $self->{name};

    my $img = $canvas->getImage();
    my $color = $img->colorExact(153,204,153);

    # TO DO - DoTS.LowComplexityNAFeature is missing

    my $sql = ("select nal.start_min, nal.end_max, a.name " .
	       "from LowComplexityNAFeature lc, ${dotsDb}.NALocation nal, ${coreDb}.Algorithm a " .
	       "where lc.na_sequence_id = $naSeqId " .
	       "and lc.na_feature_id = nal.na_feature_id " . 
	       "and nal.start_min <= $end " .
	       "and nal.end_max >= $start " .
	       "and lc.prediction_algorithm_id = a.algorithm_id " . 
	       "order by nal.start_min asc ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while (my ($x1, $x2, $name) = $sth->fetchrow_array()) { 
	my $descr = "low complexity sequence ($x1 - $x2) predicted by $name";

	$x1 = $start if ($x1 < $start);
	$x2 = $end if ($x2 > $end);

	my $args = {
	    x1 => $x1,
	    x2 => $x2,
	    height => 8,
	    color => $color,
	    filled => 1,
	    imagemapLabel => $descr,
	    imagemapTarget => '',
	    imagemapHref => '#',
	};

	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$descr';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$descr';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	push(@$spans, WDK::Model::GDUtil::Span->new($args));
    }
    $sth->finish();

    return $spans;
}

# Hexamer is a predictor of coding potential
#
sub getHexamerSpans {
    my($self, $start, $end, $canvas) = @_;

    my $dotsDb = $self->{dotsDb};

    my $fwdSpans = [];
    my $revSpans = [];

    my $img = $canvas->getImage();
    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $mapName = $self->{name};
    my $hrefFn = $self->{featureHrefFn};
    my $hrefTarget = $self->{featureHrefTarget};

    my $color1 = $img->colorExact(102,102,102);
    my $color2 = $img->colorExact(153,153,153);
    my $color3 = $img->colorExact(204,204,204);

    my $sql = ("select hf.score, nal.start_min, nal.end_max, nal.is_reversed " .
	       "from ${dotsDb}.HexamerFeature hf, ${dotsDb}.NALocation nal " .
	       "where hf.na_sequence_id = $naSeqId " .
	       "and hf.na_feature_id = nal.na_feature_id " . 
	       "and nal.start_min <= $end " .
	       "and nal.end_max >= $start " .
	       "and hf.score >= 25 ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while (my ($score, $x1, $x2, $isrev) = $sth->fetchrow_array()) { 
	my $color;

	if ($score < 100) {
	    $color = $color3;
	} elsif ($score < 200) {
	    $color = $color2;
	} else {
	    $color = $color1;
	}

	my $descr = "hexamer prediction ($x1 - $x2) with score = $score";

	$x1 = $start if ($x1 < $start);
	$x2 = $end if ($x2 > $end);

	my $args = {
	    x1 => $x1,
	    x2 => $x2,
	    height => 8,
	    color => $color,
	    filled => 1,
	    imagemapLabel => $descr,
	    imagemapTarget => '',
	    imagemapHref => '#',
	};

	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$descr';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$descr';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}
	$args->{imagemapTarget} = $hrefTarget if (defined($hrefTarget));
	$args->{imagemapHref} = &$hrefFn("$naSeqId") if (defined($hrefFn));
        my $sub_start = ($x1 - 500) >= 0 ? ($x1 - 500) : 0;
        my $start_offset = ($x1 - 500) >= 0 ? 500 : (500 - $x1);
        my $end_offset = $start_offset + ($x2 - $x1); 
        $args->{imagemapHref} .= "&start=$sub_start&end=" . ($x2 + 500) 
                              .  "&hilite_so=$start_offset&hilite_eo=$end_offset";

	if ($isrev) {
	    push(@$revSpans, WDK::Model::GDUtil::Span->new($args));
	} else {
	    push(@$fwdSpans, WDK::Model::GDUtil::Span->new($args));
	}
    }
    $sth->finish();
    return {fwd => $fwdSpans, rev => $revSpans};
}

# Generate spans representing large-scale chromosomal features.
#
sub getCentromereTelomereSpans {
    my($self, $start, $end, $canvas) = @_;

    my $dotsDb = $self->{dotsDb};

    my $img = $canvas->getImage();
    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $hrefFn = $self->{geneHrefFn};
    my $hrefTarget = $self->{geneHrefTarget};
    my $mapName = $self->{name};

    my $color = $canvas->getImage()->colorExact(51,102,204);
    my $spans = [];

    # Centromeres and telomeres
    #
    my $sql = ("select nal.start_min, nal.end_max, cf.name, cf.length, cf.percent_at " .
	       "from ${dotsDb}.ChromosomeElementFeature cf, ${dotsDb}.NAlocation nal " .
	       "where cf.na_feature_id = nal.na_feature_id " .
	       "and cf.na_sequence_id = $naSeqId " .
	       "and nal.start_min <= $end " .
	       "and nal.end_max >= $start ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();
    while(my($x1, $x2, $name, $len, $at) = $sth->fetchrow_array()) {
	$x2 = $end if ($x2 > $end);
	$x1 = $start if ($x1 < $start);
	my $centromere = ($name =~ /centromere/i);

	my $args = {
	    x1 => $x1,
	    x2 => $x2,
	    height => $centromere ? 12 : 10,
	    color => $color,
	    filled => 1,
	};

	my $descr = $centromere ? "$name length=$len percent_AT=${at}%" : "$name length=$len";
	$descr = &safeHtml($descr);
		
	if ($self->{javascript}) {
	    my $mOver = 
		("window.status = '$descr';" .
		 "document.forms['${mapName}_form']['${mapName}_defline'].value='$descr';" .
		 "return true;");
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	push(@$spans, WDK::Model::GDUtil::Span->new($args));
    }
    $sth->finish();
    return $spans;
}

# Generate vertical lines on top of the image to denote gaps in the sequence.
#
sub getScaffoldGapFeatureSpans {
    my($self, $start, $end, $canvas) = @_;

    my $dotsDb = $self->{dotsDb};

    my $img = $canvas->getImage();
    my $dbh = $self->{dbh};
    my $naSeqId = $self->{naSeqId};
    my $color = $img->colorExact(255,255,255);

# not bad:
#    my $fgColor = $img->colorExact(150,150,150);

    my $fgColor = $img->colorExact(0,0,255);
    my $spans = [];

    my $sql = ("select nal.start_min, nal.end_max " .
	       "from ScaffoldGapFeature sgf, ${dotsDb}.NALocation nal " .
	       "where sgf.na_sequence_id = $naSeqId " .
	       "and sgf.na_feature_id = nal.na_feature_id " .
	       "and nal.start_min <= $end " .
	       "and nal.end_max >= $start " .
	       "order by nal.start_min asc ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while(my($x1, $x2) = $sth->fetchrow_array()) {
	push(@$spans, WDK::Model::GDUtil::VertLineSpan->new({
	    x1 => $x1,
	    x2 => $x2,
	    color => $color,
	    fgColor => $fgColor,
	    width => 3,
	}));
    }

    $sth->finish();

    return $spans;
}

sub makeStripeSpan {
    my($self, $spans, $caption, $packer, $sort) = @_;

    my @sorted;

    if (!defined($spans)) {
	@sorted = ();
    } elsif ($sort) {
	@sorted = sort { $b->{sortScore} <=> $a->{sortScore} } @$spans;
    } else {
	@sorted = @$spans;
    }

    return WDK::Model::GDUtil::StripeSpan->new({kids => \@sorted,
				    packer => defined($packer) ? $packer : $cp0, 
				    label => $caption,
				    labelVAlign => 'center',
#				    drawBar => 1
				    });
}

sub makeSpacer {
    my($self) = @_;
    return WDK::Model::GDUtil::Span->new({height => 1,
			      shape => 'none'
			      });
}

sub makeExon {
    my($start, $end, $color, $border, $height) = @_;
    
    my $args = {
	x1 => $start,
	x2 => $end,
	height => $height ? $height : 8,
	color => $color,
	filled => 1,
	};

    $args->{border} = 1 if ($border);
    return WDK::Model::GDUtil::Span->new($args);
}

sub makeIntron {
    my($start, $end, $color, $vertoffset) = @_;
    
    my $args = {
	x1 => $start,
	x2 => $end,
	height => 1,
	vertOffset => $vertoffset ? $vertoffset : -3,
	color => $color,
	filled => 1,
	};
    
    return WDK::Model::GDUtil::Span->new($args);
}

#-------------------------------------------------
# File-scoped methods
#-------------------------------------------------

sub safeHtml {
    my($str) = @_;
    $str =~ s#<#&lt;#g;
    $str =~ s#>#&gt;#g;
    $str =~ s#'##g;
    return $str;
}
