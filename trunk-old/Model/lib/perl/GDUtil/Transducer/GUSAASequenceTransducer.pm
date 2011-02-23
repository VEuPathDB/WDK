#!/usr/bin/perl

#------------------------------------------------------------------------
# GUSAASequenceTransducer.pm
#
# Generate a clickable image for a GUS AASequence.
#
# Jonathan Crabtree
#
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::Transducer::GUSAASequenceTransducer;

use strict;

use DBI;

use WDK::Model::GDUtil::GDCanvas;
use WDK::Model::GDUtil::Span;
use WDK::Model::GDUtil::StripeSpan;
use WDK::Model::GDUtil::AxisSpan;
use WDK::Model::GDUtil::HydropathySpan;
use WDK::Model::GDUtil::ColoredSequenceSpan;
use WDK::Model::GDUtil::Packer;

#-------------------------------------------------
# Defaults
#-------------------------------------------------

my $DFLT_IMG_WIDTH = 400;
my $DFLT_IMG_HEIGHT = 300;
my $DFLT_JAVASCRIPT = 1;
my $DFLT_AUTO_HEIGHT = 1;
my $DFLT_TICK_INTERVAL = 'ends';
my $DFLT_MIN_PFAM_SCORE = 5;

#-------------------------------------------------
# GUSAASequenceTransducer
#-------------------------------------------------

sub new {
    my($class, $args) = @_;

    my $iw = $args->{imgWidth};
    my $ih = $args->{imgHeight};
    my $js = $args->{javascript};
    my $autoheight = $args->{autoheight};
    my $tickInterval = $args->{tickInterval};
    my $minPfamScore = $args->{minPfamScore};

    my $self = {
	width => (defined $iw) ? $iw : $DFLT_IMG_WIDTH,
	height => (defined $ih) ? $ih : $DFLT_IMG_HEIGHT,
	javascript => (defined $js) ? $js : $DFLT_JAVASCRIPT,
	minPfamScore => (defined $minPfamScore) ? $minPfamScore : $DFLT_MIN_PFAM_SCORE,
	aaSeqId => $args->{aaSeqId},
	aaSeqLen => $args->{aaSeqLen},
	dbh => $args->{dbh},
	autoheight => (defined $autoheight) ? $autoheight : $DFLT_AUTO_HEIGHT,
	tickInterval => (defined $tickInterval) ? $tickInterval : $DFLT_TICK_INTERVAL,
	coreDb => $args->{coreDb},
	dotsDb => $args->{dotsDb},
    };
    
    bless $self, $class;

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

    if ($self->{javascript}) {
	$html .= "<script language=\"JavaScript1.1\">\n";
	$html .= "// <!--\n";
	$html .= <<ENDFUNC;

	function show${fnName}Info(descr, location, id, score) {
	    window.status = "(" + location + ")" + " " + descr;

	    if (document.getElementById) {
		document.getElementById('${name}_description').value = descr;
		document.getElementById('${name}_location').value = location;
		document.getElementById('${name}_id').value = id;
		document.getElementById('${name}_score').value = score;
		return true;
	    } 
	    else if (document.forms) {
		document.forms['${name}_form']['${name}_description'].value = descr;
		document.forms['${name}_form']['${name}_location'].value = location;
		document.forms['${name}_form']['${name}_id'].value = id;
		document.forms['${name}_form']['${name}_score'].value = score;
		return true;
	    }
	    return false;
	}
ENDFUNC

	$html .= "// -->\n";
	$html .= "</script>\n";

	$html .= "<FORM ID=\"${name}_form\" NAME=\"${name}_form\">\n";
    }
    
    $html .= $self->getImageMap();
    $html .= "<IMG SRC=\"${imgURL}\" BORDER=\"0\" WIDTH=\"$w\" HEIGHT=\"$h\" ";
    $html .= "USEMAP=\"#${name}\">";

    # Where JavaScript mouseover information is displayed
    #
    if ($self->{javascript}) {
	$html .= "<TABLE BORDER=\"0\">\n";
	$html .= "<TR>";

	$html .= "<TD>Description:</TD>";
	$html .= "<TD COLSPAN=\"5\">";
	$html .= "<INPUT READONLY TYPE=\"text\" NAME=\"${name}_description\" ID=\"${name}_description\" SIZE=\"75\"";
	$html .= " VALUE=\"Place the mouse over a feature to see its details.\">";
	$html .= "</TD>\n";
	$html .= "<BR CLEAR=\"both\">\n";
	$html .= "</TR>\n";

	$html .= "<TR>\n";
	$html .= "<TD>Location:</TD>";
	$html .= "<TD>";
	$html .= "<INPUT READONLY TYPE=\"text\" NAME=\"${name}_location\" ID=\"${name}_location\" SIZE=\"12\"";
	$html .= " VALUE=\"\">";
	$html .= "</TD>";
	$html .= "<TD>ID:</TD>";
	$html .= "<TD>";
	$html .= "<INPUT READONLY TYPE=\"text\" NAME=\"${name}_id\" ID=\"${name}_id\" SIZE=\"20\"";
	$html .= " VALUE=\"\">";
	$html .= "</TD>";
	$html .= "<TD>Score:</TD>";
	$html .= "<TD>";
	$html .= "<INPUT READONLY TYPE=\"text\" NAME=\"${name}_score\" ID=\"${name}_score\" SIZE=\"12\"";
	$html .= " VALUE=\"\">";
	$html .= "</TD>";
	$html .= "</TR>\n";

#	$html .= "<TR>";
#	$html .= "<TD>Strand/frame:<INPUT READONLY TYPE=\"text\" NAME=\"${name}_strand\" SIZE=\"5\"></TD>\n";
#	$html .= "<TD> Identical:<INPUT READONLY TYPE=\"text\" NAME=\"${name}_pctid\" SIZE=\"3\"></TD>\n";
#	$html .= "<TD> Positive:<INPUT READONLY TYPE=\"text\" NAME=\"${name}_pctpos\" SIZE=\"3\"></TD>\n";
#	$html .= "<TD> Subject:<INPUT READONLY TYPE=\"text\" NAME=\"${name}_subject\" SIZE=\"21\"></TD>\n";
#	$html .= "<TD> length:<INPUT READONLY TYPE=\"text\" NAME=\"${name}_subjlen\" SIZE=\"7\"></TD>\n";
#	$html .= "</TR>";
	$html .= "</TABLE>\n";
	$html .= "</FORM>\n";
    }

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
    my $img = $canvas->getImage();
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

    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;
    my $aaSeqLen = $self->{'aaSeqLen'};
    
    my $canvas =  
      WDK::Model::GDUtil::GDCanvas->new($self->{width}, $self->{height},
			    {x1 => 150, x2 => $self->{width} - 25},
			    {x1 => 0, x2 => $aaSeqLen });

    $canvas->allocateWebSafePalette();

    # Collect spans
    #
    my $topLevelSpans = [];

    # AA Sequence graphic
    # 
    my $args = {x1 => 0, x2 => $aaSeqLen, 
		aaseq => $self->getSequence(), 
		height => 8, label => 'AA sequence',
	    };

    my $seqHref = $self->{'seqHref'};
    $args->{'imagemapHref'} = $seqHref if (defined($seqHref));

    if ($self->{javascript}) {
	my $sh = &safeHtml("Graphical depiction of the amino acid sequence (see color code below)");
	my $mOver = "show${fnName}Info('$sh', '1-$aaSeqLen', 'AA sequence', ''); return true;";
	$args->{imagemapOnMouseOver} = $mOver;
    }

    my $css = WDK::Model::GDUtil::ColoredSequenceSpan->new($args);
    push(@$topLevelSpans, $css);

    # Hydropathy, window size = 9
    #
    $args = {x1 => 0, x2 => $aaSeqLen, 
	     aaseq => $self->getSequence(), 
	     height => 30, label => 'hydropathy plot', windowSize => 9,
	     imagemapLabel => "Kyte Doolittle hydropathy plot with window size = 9",
	     imagemapHref => '',
	 };

    if ($self->{javascript}) {
	my $sh = &safeHtml("Kyte Doolitle hydropathy plot with window size = 9");
	my $mOver = "show${fnName}Info('$sh', '1-$aaSeqLen', 'hydropathy plot', ''); return true;";
	$args->{imagemapOnMouseOver} = $mOver;
    }

    my $hs9 = WDK::Model::GDUtil::HydropathySpan->new($args);
    push(@$topLevelSpans, $hs9);

    # Mass spec. (proteomics) features
    #
    my $massSpecSpans = $self->getMassSpecFeatureSpans($canvas);
    push(@$topLevelSpans, @$massSpecSpans);
    
    # Low complexity features
#    my $lowComplexitySpans = $self->getLowComplexityFeatureSpans($canvas);
#    push(@$topLevelSpans, @$lowComplexitySpans);
    
    # Predicted epitopes
#    my $epitopeSpans = $self->getEpitopeFeatureSpans($canvas);
#    push(@$topLevelSpans, @$epitopeSpans);

    # SNPs - from SeqVariation
#    my $snpSpans = $self->getSNPFeatureSpansFromSeqVariation($canvas);
#    push(@$topLevelSpans, @$snpSpans);

    # SNPs - from AASeqVariation
    my $snpSpansN = $self->getSNPFeatureSpansFromAASeqVariation($canvas);
    push(@$topLevelSpans, @$snpSpansN);

    # SignalP
    my $signalPSpans = $self->getSignalPeptideFeatureSpans($canvas);
    push(@$topLevelSpans, @$signalPSpans);

    # PredictedProteinFeatures (e.g. TM, PFAM, PROSITE)
    my $protFeatSpans = $self->getPredictedProteinFeatureSpans($canvas);
    push(@$topLevelSpans, @$protFeatSpans);

    my $slbl = defined($self->{seqLabel}) ? $self->{seqLabel} : "AASequence " . $self->{aaSeqId};
    my $rootSpan = 
      WDK::Model::GDUtil::AxisSpan->new({
	  x1 => 0, 
	  x2 => $aaSeqLen, 
	  y1 => $self->{height} - 5,
	  height => 6, tickHeight => 4, tickWidth => 1,
	  kids => $topLevelSpans,
	  packer => WDK::Model::GDUtil::Packer::simplePacker(2),
	  tickLabel => 'aa',
	  label => $slbl,
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
				{x1 => 0, x2 => $aaSeqLen});
	
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

sub getSequence {
    my($self) = @_;
    my $dotsDb = $self->{dotsDb};

    if (!defined($self->{'sequence'})) {
	my $aaSeqId = $self->{'aaSeqId'};
	my $dbh = $self->{'dbh'};
	$dbh->{LongReadLen} = 50000;      # should be enough for most proteins!

	my $sql = "select sequence from ${dotsDb}.aasequenceimp where aa_sequence_id = $aaSeqId";
	my $sth = $dbh->prepare($sql);
	$sth->execute();
	my($seq) = $sth->fetchrow_array();
	$sth->finish();

	$self->{'sequence'} = $seq;
    }
    return $self->{'sequence'};
}

my $cp0 = &WDK::Model::GDUtil::Packer::constantPacker(0);
my $lrp1 = &WDK::Model::GDUtil::Packer::leftToRightPacker(1);

sub getPredictedProteinFeatureSpans {
    my($self, $canvas) = @_;
    my $dotsDb = $self->{dotsDb};

    my $aaSeqId = $self->{'aaSeqId'};
    my $dbh = $self->{'dbh'};
    my $hrefTarget = $self->{'hrefTarget'};
    my $minPfamScore = $self->{'minPfamScore'};

    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;
    
    my $sql = ("select paf.aa_feature_id, paf.name, paf.source_id, paf.algorithm_name, " .
	       "       paf.description, aal.start_min, aal.end_max, " .
	       "       pe.accession, pe.identifier, pe.release, pe.definition, paf.score " .
	       "from ${dotsDb}.PredictedAAFeature paf, ${dotsDb}.AALocation aal, ${dotsDb}.PFamEntry pe " .
	       "where paf.aa_sequence_id = $aaSeqId " .
	       "and paf.aa_feature_id = aal.aa_feature_id " .
	       "and paf.pfam_entry_id = pe.pfam_entry_id (+) ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    # Spans grouped by algorithm
    #
    my $groupedSpans = {};

    while (my ($aaFeatId, $name, $srcId, $alg, $descr, 
	       $x1, $x2, $acc, $id, $rel, $def, $score) = $sth->fetchrow_array()) 
    {
	my $groupKey = $alg;
	my $packer = $cp0;
	my $hitId = '';
	my $hitScore = '';
	my $href = undef;

	# ----------------------------------------------------
	# SET SPAN DESCRIPTION, PACKER AND HREF - factor this out
	# ----------------------------------------------------

	my $desc = undef;

	if ($alg =~ /tmap|tmpred|tmhmm|toppred/i) {            # TM domains
	    $desc = "$descr predicted by $alg";
	    $href = "";
	    $hitId = $alg;
	} elsif ($alg =~ /pfam/i) {                            # PFAM
	    $desc = "PFAM $rel:$acc:$descr";
	    $groupKey = ($id =~ /\S/) ? "PF $id" : "PF $descr";
	    $packer = $cp0;
	    $hitId = $id;
	    $hitScore = $score;
	    $href = "http://pfam.wustl.edu/cgi-bin/getdesc?acc=$acc";

	    # Filter low-scoring PFAM hits
	    #
	    next if ($hitScore < $minPfamScore);
	} elsif ($alg =~ /patmatmotifs/i) {                    # PROSITE
	    my($name, $id) = ($descr =~ /PROSITE motif (\S+), accession (\S+)/);
	    $desc = "PROSITE $id:$name";
	    $groupKey = "PS $name";
	    $packer = $cp0;
	    $hitId = $id;
	    $href = "http://us.expasy.org/cgi-bin/nicesite.pl?$id";
	}
	else { #default
	    $desc = "$srcId:$descr";
	    $hitId = $srcId;
	    $href = "";
	}

	# ----------------------------------------------------

	# ----------------------------------------------------
	# SET COLOR - factor this out

	# Set color to match Martin's protein graphics
	#
	my $img = $canvas->getImage();
	my $color = $img->colorExact(204,204,204);

	if ($alg =~ /tmap|tmpred|tmhmm|toppred/i) {
	    $color = $img->colorExact(255,0,255);
	} elsif ($alg =~ /tmhelix/i) {
	    $color = $img->colorExact(153,204,255);
	} elsif ($alg =~ /sheet|signalp/i) {
	    $color = $img->colorExact(255,0,0);
	} elsif ($alg =~ /pfam/i) {
	    $color = $img->colorExact(255,153,0);
	} elsif ($alg =~ /patmatmotifs/i) {
	    $color = $img->colorExact(0,255,0);
	}
	# ----------------------------------------------------

	my $args = {
	    x1 => $x1,
	    x2 => $x2,
	    height => 10,
	    color => $color,
	    filled => 1,
	    border => 1,
	    imagemapHref => $href,
	    imagemapTarget => $hrefTarget,
	};

	if ($self->{javascript}) {
	    my $sh = &safeHtml($desc);
	    my $mOver = "show${fnName}Info('$sh', '$x1-$x2', '$hitId', '$hitScore'); return true;";
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	my $span = WDK::Model::GDUtil::Span->new($args);

	# Add to list of spans for this algorithm type
	#
	my $group = $groupedSpans->{$groupKey};

	if (defined($group)) {
	    my $spans = $group->{'spans'};
	    push(@$spans, $span);
	} else {
	    $groupedSpans->{$groupKey} = 
	    { 
		spans => [$span], packer => $packer, 
		href => $href, hrefTarget => $hrefTarget 
		};
	}
    }
    $sth->finish();

    # Make a StripeSpan for each collection of like features
    #
    my $stripeSpans = [];
    my @sortedKeys = sort { $b cmp $a } keys %$groupedSpans;

    foreach my $key (@sortedKeys) {
	my $group = $groupedSpans->{$key};
	my $spans = $group->{'spans'};
	my $packer = $group->{'packer'};
	my $href = $group->{'href'};
	my $hrefTarget = $group->{'hrefTarget'};

	# Determine packer - one line or many
	#
	my $ss = WDK::Model::GDUtil::StripeSpan->new({
	    kids => $spans,
	    packer => $packer,
	    label => $key,
	    labelVAlign => 'center',
	    imagemapHref => $href,
	    imagemapHrefTarget => $hrefTarget,
	});
	push(@$stripeSpans, $ss);
    }

    # TO DO - allow one to configure order of StripeSpans?

    return $stripeSpans;
}

sub getSignalPeptideFeatureSpans {
    my($self, $canvas) = @_;
    my $dotsDb = $self->{dotsDb};

    my $aaSeqId = $self->{'aaSeqId'};
    my $dbh = $self->{'dbh'};
    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;
    
    my $sql = ("select spf.aa_feature_id, spf.algorithm_name, spf.num_positives, " .
	       "       aal.start_min, aal.end_max, " .
	       "       spf.maxy_score, spf.maxc_score, spf.maxs_score, spf.means_score " .
	       "from ${dotsDb}.SignalPeptideFeature spf, ${dotsDb}.AALocation aal " .
	       "where spf.aa_sequence_id = $aaSeqId " .
	       "and spf.aa_feature_id = aal.aa_feature_id " .
	       "order by aa_feature_id ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $spans = [];

    # ----------------------------------------------------
    # SET COLOR - factor this out

    my $img = $canvas->getImage();
    my $red = $img->colorExact(255,0,0);
    my $pink = $img->colorExact(255,153,204);
    # ----------------------------------------------------

    while (my($aaFeatId, $alg, $np, $x1, $x2, $maxy, $maxc, $maxs, $means) = $sth->fetchrow_array()) {
	my $descr = "$alg positives=$np/4 max_y=$maxy max_c=$maxc max_s=$maxs mean_s=$means";
	
	my $args = {
	    x1 => $x1,
	    x2 => $x2,
	    height => 10,
	    color => ($np == 4) ? $red : $pink,    # HACK
	    filled => 1,
	    border => 1,
	};
	
	if ($self->{javascript}) {
	    my $sh = &safeHtml($descr);
	    my $mOver = "show${fnName}Info('$sh', '$x1-$x2', 'SignalP', '${np}/4'); return true;";
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	push(@$spans, WDK::Model::GDUtil::Span->new($args));
    }
    $sth->finish();

    if (scalar(@$spans) > 0) {
	my $ss = WDK::Model::GDUtil::StripeSpan->new({
	    kids => $spans,
	    packer => $cp0,
	    label => 'signal peptide',
	    labelVAlign => 'center',
	});
	
	return [$ss];
    }
    return [];
}

sub getMassSpecFeatureSpans {
    my($self, $canvas) = @_;
    my $coreDb = $self->{coreDb};
    my $dotsDb = $self->{dotsDb};

    my $aaSeqId = $self->{'aaSeqId'};
    my $dbh = $self->{'dbh'};
    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;

    my $img = $canvas->getImage();
    my $red = $img->colorExact(255,0,0);

    # TO DO - dots.MassSpecFeature doesn't exist

    # Retrieve all MassSpecFeatures; currently there's only one dataset   
    # and so we group only by developmental_stage
    #
    my $sql = ("select aal.start_min, aal.end_max, a.name, msf.developmental_stage " .
	       "from ${dotsDb}.MassSpecFeature msf, ${dotsDb}.AALocation aal, ${coreDb}.Algorithm a " .
	       "where msf.aa_sequence_id = $aaSeqId " .
	       "and aal.aa_feature_id = msf.aa_feature_id " .
	       "and msf.prediction_algorithm_id = a.algorithm_id " .
	       "order by msf.prediction_algorithm_id, " .
	       " msf.developmental_stage, " .
	       " aal.start_min ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $rowsByStage = {};

    while (my @row = $sth->fetchrow_array()) {
	my $list = $rowsByStage->{$row[3]};
	my @copy = @row;

	if (!defined($list)) {
	    $rowsByStage->{$row[3]} = [\@copy];
	} else {
	    push(@$list, \@copy);
	}
    }

    $sth->finish();
    my $spans = [];
    
    foreach my $stage (keys %$rowsByStage) {
	my $rows = $rowsByStage->{$stage};
	my $stageSpans = [];

	foreach my $row (@$rows) {
	    my($start, $end, $alg, $stage) = @$row;

	    my $args = {
		x1 => $start,
		x2 => $end,
		height => 10,
		color => $red,
		filled => 1
	    };

	    my $descr = "$stage stage peptide fragment(s) detected by mass spectrometry";

	    if ($self->{javascript}) {
		my $sh = &safeHtml($descr);
		my $mOver = "show${fnName}Info('$sh', '$start-$end', 'Mass spec. span', ''); return true;";
		$args->{imagemapOnMouseOver} = $mOver;
	    }

	    push(@$stageSpans, WDK::Model::GDUtil::Span->new($args));
	}

	$stage =~ tr/A-Z/a-z/;
	my $ssDescr = "MS $stage";
	$ssDescr =~ tr/a-z/A-Z/;

	my $ss = WDK::Model::GDUtil::StripeSpan->new({
	    kids => $stageSpans,
	    packer => $cp0,
	    label => $ssDescr,
	    labelVAlign => 'bottom'
	});
	push(@$spans, $ss);
    }

    return $spans;
}

sub getEpitopeFeatureSpans {
    my($self, $canvas) = @_;
    my $dotsDb = $self->{dotsDb};

    my $aaSeqId = $self->{'aaSeqId'};
    my $dbh = $self->{'dbh'};
    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;
    my $aaSeq = $self->getSequence();

    my $img = $canvas->getImage();
    my $color = $img->colorExact(153,153,204);

    # TO DO - dots.EpitopeFeature doesn't exist yet

    my $sql = ("select aal.start_min, aal.end_max, " .
	       "ef.haplotype, ef.type, ef.score, ef.max_score, ef.width " .
	       "from ${dotsDb}.EpitopeFeature ef, ${dotsDb}.AALocation aal " .
	       "where ef.aa_sequence_id = $aaSeqId " .
	       "and ef.aa_feature_id = aal.aa_feature_id " .
	       "order by aal.start_min asc ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    my $spans = [];

    while (my($start, $end, $hap, $type, $score, $max, $wid) = $sth->fetchrow_array()) {
	my $sl = $end - $start + 1;
	my $lseq = substr($aaSeq, $start - 1, $sl);

	my $args = {
	    x1 => $start,
	    x2 => $end,
	    height => 10,
	    color => $color,
	    filled => 1,
	};
	
	if ($self->{javascript}) {
	    my $sh = &safeHtml("Predicted CD8 $hap $type epitope: $lseq");
	    my $mOver = "show${fnName}Info('$sh', '$start-$end', '$hap $type', '$score/$max'); return true;";
	    $args->{imagemapOnMouseOver} = $mOver;
	}
	push(@$spans, WDK::Model::GDUtil::Span->new($args));
    }

    $sth->finish();
    
    my $ss = WDK::Model::GDUtil::StripeSpan->new({
	kids => $spans,
	packer => $cp0,
	label => 'predicted epitopes',
	labelVAlign => 'center',
    });
    return [$ss];
}

sub getLowComplexityFeatureSpans {
    my($self, $canvas) = @_;
    my $dotsDb = $self->{dotsDb};
    my $coreDb = $self->{coreDb};

    my $aaSeqId = $self->{'aaSeqId'};
    my $dbh = $self->{'dbh'};
    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;
    my $aaSeq = $self->getSequence();

    my $img = $canvas->getImage();
    my $color = $img->colorExact(153,204,153);

    # TO DO - dots.lowcomplexityfeature doesn't exist yet

    my $sql = ("select aal.start_min, aal.end_max, a.name " .
	       "from ${dotsDb}.LowComplexityAAFeature lc, ${dotsDb}.AALocation aal, ${coreDb}.Algorithm a " .
	       "where lc.aa_sequence_id = $aaSeqId " .
	       "and aal.aa_feature_Id = lc.aa_feature_id " .
	       "and lc.prediction_algorithm_id = a.algorithm_id " .
	       "order by aal.start_min asc ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    my $spans = [];

    while (my($start, $end, $alg) = $sth->fetchrow_array()) {
	my $sl = $end - $start + 1;
	my $lseq = substr($aaSeq, $start - 1, $sl);

	my $args = {
	    x1 => $start,
	    x2 => $end,
	    height => 10,
	    color => $color,
	    filled => 1,
	};
	
	if ($self->{javascript}) {
	    my $sh = &safeHtml("Low complexity sequence ($alg): $lseq");
	    my $mOver = "show${fnName}Info('$sh', '$start-$end', 'low complexity ($alg)', 'length=$sl'); return true;";
	    $args->{imagemapOnMouseOver} = $mOver;
	}
	push(@$spans, WDK::Model::GDUtil::Span->new($args));
    }

    $sth->finish();
    
    my $ss = WDK::Model::GDUtil::StripeSpan->new({
	kids => $spans,
	packer => $cp0,
	label => 'low complexity seq.',
	labelVAlign => 'bottom',
    });
    return [$ss];
}

sub getSNPFeatureSpansFromAASeqVariation {
    my($self, $canvas) = @_;
    my $dotsDb = $self->{dotsDb};

    my $aaSeqId = $self->{'aaSeqId'};
    my $dbh = $self->{'dbh'};
    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;

    my $img = $canvas->getImage();
    my $blue = $img->colorExact(153,153,255);
    my $red = $img->colorExact(255,0,0);

    # TO DO - dots.aaseqvariation doesn't exist yet

    # First retrieve all SNP features for this gene
    #
    my $sql = ("select aal.start_min, sv.strain, sv.original, sv.substitute, sv.genomic_locn " .
	       "from ${dotsDb}.AASeqVariation sv, ${dotsDb}.AALocation aal " .
	       "where sv.aa_sequence_id = $aaSeqId " .
	       "and aal.aa_feature_Id = sv.aa_feature_id " .
	       "order by aal.start_min asc, sv.strain asc ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $snpsByLocn = {};

    # Group SNPs by genomic_location
    # 
    while (my $hr = $sth->fetchrow_hashref('NAME_lc')) {
	my %copy = %$hr;
	my $locn = $hr->{'genomic_locn'};
	my $snps = $snpsByLocn->{$locn};

	if (defined($snps)) {
	    push(@$snps, \%copy);
	} else{
	    $snpsByLocn->{$locn} = [\%copy];
	}
    }
    $sth->finish();

    my $snpSpans = [];
    my @sortedLocns = sort { $a <=> $b } keys %$snpsByLocn;

    foreach my $genomicLocn (@sortedLocns) {
	my $descr = '';
	my $snps = $snpsByLocn->{$genomicLocn};
	my $isSynonymous = 1;
	my $aaLocn = $snps->[0]->{start_min};

	foreach my $snp (@$snps) {
	    my $strain = $snp->{'strain'};
	    my $orig = $snp->{'original'};
	    my $aa = $snp->{'substitute'};
	    $descr .= "$strain=>$aa ";
	    $isSynonymous = 0 if ($orig ne $aa);
	}

	my $color = undef;
	if ($isSynonymous) {
	    $descr = "(synonymous)";
	    $color = $blue;
	} else {
	    $color = $red;
	}
	
	my $args = {
	    x1 => $aaLocn - 1,
	    x2 => $aaLocn + 1,
	    height => 10,
	    color => $color,
	    filled => 1,
	};

	if ($self->{javascript}) {
	    my $sh = &safeHtml("SNP " . $descr);
	    my $mOver = "show${fnName}Info('$sh', '$aaLocn-$aaLocn', 'SNP', ''); return true;";
	    $args->{imagemapOnMouseOver} = $mOver;
	}
	push(@$snpSpans, WDK::Model::GDUtil::Span->new($args));
    }

    if (scalar(@$snpSpans) > 0) {
	my $ss = WDK::Model::GDUtil::StripeSpan->new({
	    kids => $snpSpans,
	    packer => $cp0,
	    label => 'SNPs',
	    labelVAlign => 'bottom',
	});
	return [$ss];
    }
    return [];
}

sub getSNPFeatureSpansFromSeqVariation {
    my($self, $canvas) = @_;
    my $dotsDb = $self->{dotsDb};

    my $aaSeqId = $self->{'aaSeqId'};
    my $dbh = $self->{'dbh'};
    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;

    my $img = $canvas->getImage();
    my $blue = $img->colorExact(0,0,255);

    # First retrieve all SNP features for this gene
    #
    my $sql = ("select nal.start_min, sv.strain, sv.substitute " .
	       "from ${dotsDb}.TranslatedAAFeature taf, ${dotsDb}.RNAFeature rnaf, ${dotsDb}.GeneFeature gf, " .
	       "     ${dotsDb}.SeqVariation sv, ${dotsDb}.NALocation nal " .
	       "where taf.aa_sequence_id = $aaSeqId " .
	       "and rnaf.na_feature_id = taf.na_feature_id " .
	       "and rnaf.parent_id = gf.na_feature_id " .
	       "and gf.na_sequence_id = sv.na_sequence_id " .
	       "and gf.source_id = sv.gene " .
	       "and sv.name = 'SNP' " .
	       "and nal.na_feature_id = sv.na_feature_id " .
	       "order by nal.start_min asc, sv.strain asc ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $snpsByLocn = {};

    # Group SNPs by location
    # 
    while (my $hr = $sth->fetchrow_hashref('NAME_lc')) {
	my %copy = %$hr;
	my $locn = $hr->{'start_min'};
	my $snps = $snpsByLocn->{$locn};

	if (defined($snps)) {
	    push(@$snps, \%copy);
	} else{
	    $snpsByLocn->{$locn} = [\%copy];
	}
    }
    $sth->finish();

    # If we have SNPs then retrieve the gene's exons
    #
    if (scalar(keys %$snpsByLocn) > 0) {
	my $exons = $self->getExons();
	
	# Now use the exons to map the SNPs onto the predicted AA sequence
	# TO DO - flag nonsynonymous changes 
	#
	my $aaLocn = 1;
	my $num = 1;
	my $isrev = undef;

	foreach my $e (@$exons) {
	    $isrev = $e->{is_reversed};
	    my $start = $e->{start_min};
	    my $end = $e->{end_max};
	    my $cs = $e->{coding_start};
	    my $ce = $e->{coding_end};
	    my $num = $e->{order_num};

	    my $offset = int(($cs - 1) / 3);
	    $aaLocn += $offset;

	    $e->{'genomic_start'} = $start + $cs - 1;
	    $e->{'aa_start'} = $aaLocn;
 	    $aaLocn += int((abs($ce - $cs) + 1) / 3.0);
	    $e->{'aa_end'} = $aaLocn - 1;
	    $e->{'genomic_end'} = $start + abs($ce - $cs);

#	    print STDERR "GUSAASeqTransducer: exon $num $start-$end cs=$cs ce-$ce ";
#	    print STDERR "genomic=", $e->{'genomic_start'}, "-", $e->{'genomic_end'};
#	    print STDERR " aa=", $e->{'aa_start'}, "-", $e->{'aa_end'}, "\n";

	    ++$num;
	}

	my $snpSpans = [];
	my @sortedLocns = sort { $a <=> $b } keys %$snpsByLocn;

	foreach my $locn (@sortedLocns) {
	    my $aaLocn = undef;

	    # Convert location
	    #
	    foreach my $e (@$exons) {
		my $gs = $e->{'genomic_start'};
		my $ge = $e->{'genomic_end'};

		if (($locn >= $gs) && ($locn <= $ge)) {
		    my $as = $e->{'aa_start'};

		    if ($isrev) {
			$aaLocn = $as + int(abs(($ge - $locn))/3.0);
		    } else {
			$aaLocn = $as + int((abs($locn - $gs))/3.0);
		    }
		    last;
		}
	    }

	    if (defined($aaLocn)) {
#		print STDERR "GUSAASequenceTransducer: converted SNP location $locn -> $aaLocn\n";
		
		# Generate description
		#
		my $descr = "SNP ";
		my $snps = $snpsByLocn->{$locn};
		foreach my $snp (@$snps) {
		    my $strain = $snp->{'strain'};
		    my $base = $snp->{'substitute'};
		    $descr .= "$strain=>$base ";
		}

		my $args = {
		    x1 => $aaLocn - 1,
		    x2 => $aaLocn + 1,
		    height => 10,
		    color => $blue,
		    filled => 1,
		};

		if ($self->{javascript}) {
		    my $sh = &safeHtml($descr);
		    my $mOver = "show${fnName}Info('$sh', '$aaLocn-$aaLocn', 'SNP', ''); return true;";
		    $args->{imagemapOnMouseOver} = $mOver;
		}

		push(@$snpSpans, WDK::Model::GDUtil::Span->new($args));
	    } else {
		print STDERR "GUSAASequenceTransducer: unable to determine location for SNP at $locn\n";
	    }
	}
	
	my $ss = WDK::Model::GDUtil::StripeSpan->new({
	    kids => $snpSpans,
	    packer => $lrp1,
	    label => 'SNPs',
	    labelVAlign => 'bottom'
	});

	return [$ss];
    }
    return [];
}

sub getExons {
    my($self) = @_;
    my $dotsDb = $self->{dotsDb};

    my $aaSeqId = $self->{'aaSeqId'};
    my $dbh = $self->{'dbh'};

    my $sql = ("select nal.start_min, nal.end_max, nal.is_reversed, ef.coding_start, ef.coding_end " .
	       "from ${dotsDb}.TranslatedAAFeature taf, ${dotsDb}.RNAFeature rnaf, " .
	       "     ${dotsDb}.ExonFeature ef, ${dotsDb}.NALocation nal " .
	       "where taf.aa_sequence_id = $aaSeqId " .
	       "and rnaf.na_feature_id = taf.na_feature_id " .
	       "and rnaf.parent_id = ef.parent_id " .
	       "and nal.na_feature_id = ef.na_feature_id " .
	       "order by ef.order_number asc ");
    
    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $exons = [];

    while(my $hr = $sth->fetchrow_hashref('NAME_lc')) {
	my %copy = %$hr;
	push(@$exons, \%copy);
    }
    $sth->finish();

    return $exons;
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
