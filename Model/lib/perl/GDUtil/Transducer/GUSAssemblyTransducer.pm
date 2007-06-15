#!/usr/bin/perl

#---------------------------------------------------------------------
# GUSAssemblyTransducer.pm
#
# Generate a clickable image for a GUS Assembly.
#
# Created: Wed Dec 11 09:57:05 EST 2002
#
# Jonathan Crabtree
#
# $Revision$ $Date$ $Author$
#---------------------------------------------------------------------

package WDK::Model::GDUtil::Transducer::GUSAssemblyTransducer;

use strict;

use DBI;

use WDK::Model::GDUtil::GDCanvas;
use WDK::Model::GDUtil::Span;
use WDK::Model::GDUtil::StripeSpan;
use WDK::Model::GDUtil::AxisSpan;
use WDK::Model::GDUtil::PercentATSpan;
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

#-------------------------------------------------
# GUSAssemblyTransducer
#-------------------------------------------------

sub new {
    my($class, $args) = @_;

    my $iw = $args->{imgWidth};
    my $ih = $args->{imgHeight};
    my $js = $args->{javascript};
    my $autoheight = $args->{autoheight};
    my $tickInterval = $args->{tickInterval};

    # Whether to activate experimental features currently under development
    #
    my $devFeatures = $args->{devFeatures};

    my $self = {
	width => (defined $iw) ? $iw : $DFLT_IMG_WIDTH,
	height => (defined $ih) ? $ih : $DFLT_IMG_HEIGHT,
	javascript => (defined $js) ? $js : $DFLT_JAVASCRIPT,
	naSeqId => $args->{naSeqId},
	naSeqLen => $args->{naSeqLen},
	dbh => $args->{dbh},
	autoheight => (defined $autoheight) ? $autoheight : $DFLT_AUTO_HEIGHT,
	tickInterval => (defined $tickInterval) ? $tickInterval : $DFLT_TICK_INTERVAL,
	devFeatures => $devFeatures,
	coreDb => $args->{coreDb},
	sresDb => $args->{sresDb},
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
	$html .= "<INPUT READONLY TYPE=\"text\" NAME=\"${name}_location\" ID=\"${name}_location\" SIZE=\"15\"";
	$html .= " VALUE=\"\">";
	$html .= "</TD>";
	$html .= "<TD>ID:</TD>";
	$html .= "<TD>";
	$html .= "<INPUT READONLY TYPE=\"text\" NAME=\"${name}_id\" ID=\"${name}_id\" SIZE=\"20\"";
	$html .= " VALUE=\"\">";
	$html .= "</TD>";
	$html .= "<TD>Score:</TD>";
	$html .= "<TD>";
	$html .= "<INPUT READONLY TYPE=\"text\" NAME=\"${name}_score\" ID=\"${name}_score\" SIZE=\"15\"";
	$html .= " VALUE=\"\">";
	$html .= "</TD>";
	$html .= "</TR>\n";

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
    my $naSeqLen = $self->{'naSeqLen'};
    
    my $canvas =  
      WDK::Model::GDUtil::GDCanvas->new($self->{width}, $self->{height},
			    {x1 => 150, x2 => $self->{width} - 25},
			    {x1 => 0, x2 => $naSeqLen });

    $canvas->allocateWebSafePalette();
    my $lblue = $canvas->getImage()->colorClosest(150,150,255);

    # Collect spans
    #
    my $topLevelSpans = [];
    my $slbl = defined($self->{seqLabel}) ? $self->{seqLabel} : ("DT." . $self->{naSeqId});

    if (0) {

	# AA Sequence graphic
	# 
	my $args = {x1 => 1, x2 => $naSeqLen, 
		    aaseq => $self->getSequence('sequence'), 
		    height => 8, label => 'RNA sequence',
		};
	
	my $seqHref = $self->{'seqHref'};
	$args->{'imagemapHref'} = $seqHref if (defined($seqHref));
	
	if ($self->{javascript}) {
	    my $sh = &safeHtml("Graphical depiction of the RNA sequence (see color code below)");
	    my $mOver = "show${fnName}Info('$sh', '1-$naSeqLen', 'RNA sequence', ''); return true;";
	    $args->{imagemapOnMouseOver} = $mOver;
	}
	
	my $css = WDK::Model::GDUtil::ColoredSequenceSpan->new($args);
	push(@$topLevelSpans, $css);
    }

    push(@$topLevelSpans, &makeSpacerSpan(3));

    # GC content
    # 
    my $args = {
	x1 => 1, x2 => $naSeqLen, 
	naseq => $self->getSequence('sequence'), 
	height => 30, label => '%AT plot (W=20)', windowSize => 20,
	imagemapLabel => "Percent-AT plot with window size = 20bp",
	imagemapHref => '',
	lineColor => $lblue,
    };
	
    if ($self->{javascript}) {
	my $sh = &safeHtml("Percent-AT plot with window size = 20bp");
	my $mOver = "show${fnName}Info('$sh', '1-$naSeqLen', 'percent-AT plot', ''); return true;";
	$args->{imagemapOnMouseOver} = $mOver;
	}
	
    my $at20 = WDK::Model::GDUtil::PercentATSpan->new($args);
    push(@$topLevelSpans, $at20);
    push(@$topLevelSpans, &makeSpacerSpan(3));

    # Gene trap insertion tag sequences
    #
#    my $gtSpans = $self->getGeneTrapSpans($canvas);
#    push(@$topLevelSpans, @$gtSpans);
#    push(@$topLevelSpans, &makeSpacerSpan(3));

    # RH markers
    #
    my $rhSpans = $self->getRHMapSpans($canvas);
    push(@$topLevelSpans, @$rhSpans);
    push(@$topLevelSpans, &makeSpacerSpan(3));

    # Experimental features
    #
    if ($self->{'devFeatures'}) {

	# Graphical depiction of predicted protein
	#
	my $aaSpans = $self->getTranslatedAAFeatureSpans($canvas);
	push(@$topLevelSpans, @$aaSpans);

	# Graphical depiction of ESTs and mRNAs
	#
	#my $estSpans = $self->getInputSeqSpans($canvas);
	#push(@$topLevelSpans, @$estSpans);

    }

    # Root span
    #
    my $rootSpan = 
      WDK::Model::GDUtil::AxisSpan->new({
	  x1 => 0, 
	  x2 => $naSeqLen, 
	  y1 => $self->{height} - 5,
	  height => 6, tickHeight => 4, tickWidth => 1,
	  kids => $topLevelSpans,
	  packer => WDK::Model::GDUtil::Packer::simplePacker(2),
	  tickLabel => 'bp',
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
				{x1 => 0, x2 => $naSeqLen});
	
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
    my($self, $seqType) = @_;
    my $naSeqId = $self->{'naSeqId'};
    my $dotsDb = $self->{dotsDb};

    if (!defined($self->{$seqType})) {
	my $aaSeqId = $self->{'naSeqId'};
	my $dbh = $self->{'dbh'};
	$dbh->{LongReadLen} = 500000;      # should be enough for most proteins!

	my $sql = "select $seqType from ${dotsDb}.Assembly where na_sequence_id = $naSeqId";
	my $sth = $dbh->prepare($sql);
	$sth->execute();
	my($seq) = $sth->fetchrow_array();
	$sth->finish();

	$self->{$seqType} = $seq;
    }
    return $self->{$seqType};
}

my $cp0 = &WDK::Model::GDUtil::Packer::constantPacker(0);
my $lrp1 = &WDK::Model::GDUtil::Packer::leftToRightPacker(1);
my $lrp2 = &WDK::Model::GDUtil::Packer::leftToRightPacker(2);

# Generate spans corresponding to the entries in GeneTrapAssembly
#
sub getGeneTrapSpans {
    my($self, $canvas) = @_;
    my $spans = [];

    my $sresDb = $self->{sresDb};
    my $dotsDb = $self->{dotsDb};

    my $naSeqId = $self->{'naSeqId'};
    my $dbh = $self->{'dbh'};
    my $hrefTarget = $self->{'hrefTarget'};
    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;

    my $img = $canvas->getImage();
    my $black = $img->colorClosest(0,0,0);
    my $lblue = $img->colorClosest(150,150,255);
    my $mblue = $img->colorClosest(100,100,255);
    my $dblue = $img->colorClosest(50,50,255);

    my $sql = ("select gta.is_best_match, gta.match_start, gta.match_end, gta.is_reversed, gta.percent_identity, " .
	       "       ena.source_id, ena.secondary_identifier, ed.name " .
	       "from ${dotsDb}.GeneTrapAssembly gta, ${dotsDb}.ExternalNASequence ena, " .
	       "     ${sresDb}.ExternalDatabaseRelease edr, ${sresDb}.ExternalDatabase ed " .
	       "where gta.assembly_na_sequence_id = $naSeqId " .
	       "and gta.tag_na_sequence_id = ena.na_sequence_id " . 
	       "and ena.external_database_release_id = edr.external_database_release_id " .
	       "and edr.external_database_id = ed.external_database_id " .
	       "order by edr.external_database_release_id, gta.match_start ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while (my($bm,$start,$end,$isrev,$pct,$src,$sec,$dbname) = $sth->fetchrow_array()) {

	my $href;
	my $spanDescr;
	
	if ($dbname =~ /skarnes/i) {
	    my $lcSrcId = $src;
	    $lcSrcId =~ tr/A-Z/a-z/;
	    $href = "http://socrates.berkeley.edu/~skarnes/${lcSrcId}.html";
	    $spanDescr = "gene trap $src";
	} elsif ($dbname =~ /ggtc/i) {

	    # Clone (secondary_identifier)
	    #
	    $href = "http://tikus.gsf.de/project/web_new/database/result_clone.html?clone_id=$sec";

	    # Sequence (source_id)
#	    $href = "http://tikus.gsf.de/project/sequence/${src}q.html";

	    $spanDescr = "gene trap $sec";
	} else {
	    $href = "#";
	    $spanDescr = "gene trap $src";
	}

	my $color;
	if ($pct >= 99) {
	    $color = $dblue;
	} elsif ($pct >= 95) {
	    $color = $mblue;
	} else {
	    $color = $lblue;
	}

	my $args = {
	    x1 => $start,
	    x2 => $end,
	    height => 8,
	    color => $color,
	    borderColor => $black,
	    filled => 1,
	    border => 1,
	    imagemapHref => $href,
	    imagemapTarget => $hrefTarget,
	};
	
	if ($self->{javascript}) {
	    my $sh = &safeHtml("$src ($dbname)");
	    my $sym = $isrev ? '-' : '+';
	    my $id = $src;
	    $id .= "[$sec]" if ($sec =~ /\S/);
	    my $mOver = "show${fnName}Info('$sh', '$start-$end ($sym)', '$id', '${pct}% identity'); return true;";
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	my $span = WDK::Model::GDUtil::Span->new($args);

	my $ss = WDK::Model::GDUtil::StripeSpan->new({
	    kids => [$span],
	    packer => $cp0,
	    label => $spanDescr,
	    labelVAlign => 'center',
	});

	push(@$spans, $ss);
    }

    $sth->finish();
    return $spans;
}

sub getRHMapSpans {
    my($self, $canvas) = @_;
    my $spans = [];

    my $sresDb = $self->{sresDb};
    my $dotsDb = $self->{dotsDb};

    my $naSeqId = $self->{'naSeqId'};
    my $dbh = $self->{'dbh'};
    my $hrefTarget = $self->{'hrefTarget'};
    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;

    my $img = $canvas->getImage();
    my $black = $img->colorClosest(0,0,0);
    my $dred = $img->colorClosest(255,50,50);

    my $sql = ("select distinct epcr.start_pos, epcr.stop_pos, rhm.genbank_id, rm.name, rhmm.chromosome, " .
	       "TO_CHAR(rhmm.centirays, 'FM9999.999') as centirays, rhmm.panel_description, rhmm.lab " .
	       "from ${dotsDb}.EPCR epcr, ${dotsDb}.RHMapMarker rhmm, ${dotsDb}.RHMarker rhm, ${dotsDb}.RHMap rm " .
	       "where epcr.na_sequence_id = $naSeqId " .
	       "and epcr.map_table_id = 2782 " .
	       "and epcr.map_id = rhm.rh_marker_id " .
	       "and rhm.rh_marker_id = rhmm.rh_marker_id " .
	       "and rhmm.rh_map_id = rm.rh_map_id " .
	       "order by rhmm.panel_description, epcr.start_pos ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while (my($start,$end,$accn,$name,$chrom,$cr,$panel,$lab) = $sth->fetchrow_array()) {

	my $href;

	if ($accn =~ /\S/) {
	  $href = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=Nucleotide&cmd=Search&term=$accn&doptcmdl=GenBank";
	} else {
	    $href = "#";
	}

	my $args = {
	    x1 => $start,
	    x2 => $end,
	    height => 8,
	    color => $dred,
	    borderColor => $black,
	    filled => 1,
	    border => 1,
	    imagemapHref => $href,
	    imagemapTarget => $hrefTarget,
	};
	
	if ($self->{javascript}) {
	    $name .= " ($accn)" if ($accn =~ /\S/);
	    my $descr = "RH marker $name: chr. $chrom, $cr centirays";
	    $descr .= " ($panel)" if ($panel =~ /\S/);
	    my $sh = &safeHtml($descr);
	    my $mOver = "show${fnName}Info('$sh', '$start-$end', '$name', ''); return true;";
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	my $span = WDK::Model::GDUtil::Span->new($args);

	my $ss = WDK::Model::GDUtil::StripeSpan->new({
	    kids => [$span],
	    packer => $cp0,
	    label => "RH map: chr. $chrom",
	    labelVAlign => 'center',
	});

	push(@$spans, $ss);
    }
    $sth->finish();
    return $spans;
}

sub getInputSeqSpans {
    my($self, $canvas) = @_;
    my $spans = [];

    my $sresDb = $self->{sresDb};
    my $dotsDb = $self->{dotsDb};

    my $naSeqId = $self->{'naSeqId'};
    my $dbh = $self->{'dbh'};
    my $hrefTarget = $self->{'hrefTarget'};
    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;

    my $img = $canvas->getImage();
    my $white = $img->colorClosest(255,255,255);
    my $black = $img->colorClosest(0,0,0);
    my $mblue = $img->colorClosest(100,100,255);
    my $mred = $img->colorClosest(255,100,100);
    my $mgrey = $img->colorClosest(100,100,100);
    my $lgrey = $img->colorClosest(150,150,150);

    my $gappedSeq = $self->getSequence('gapped_consensus');
    my $gapsByPosn = [];
    my $gappedSeqLen = length($gappedSeq);
    my $gapCount = 0;

    for (my $i = 0;$i < $gappedSeqLen;++$i) {
	$gapsByPosn->[$i] = $gapCount;
	$gapCount++ if (substr($gappedSeq, $i, 1) eq '-');
    }

    my $sql = ("select aseq.assembly_offset, aseq.assembly_strand, aseq.sequence_start, aseq.sequence_end, " .
	       "aseq.gapped_sequence, ena.source_id, ena.description, ena.length, ls.p_end, ls.washu_id, st.name " .
	       "from ${dotsDb}.AssemblySequence aseq, ${dotsDb}.ExternalNASequence ena, " .
	       "     ${dotsDb}.EST ls, ${dotsDb}.SequenceType st " .
	       "where aseq.assembly_na_sequence_id = $naSeqId " .
	       "and aseq.na_sequence_id = ena.na_sequence_id " .
	       "and ena.na_sequence_id = ls.na_sequence_id (+) " .
	       "and ena.sequence_type_id = st.sequence_type_id " .
	       "order by st.name, aseq.sequence_start ");

    $dbh->{'LongReadLen'} = 500000;
    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while (my($offset,$strand,$ss,$se,$gaps,$src,$defline,$seqLen,$pend,$washu,$stype) = $sth->fetchrow_array()) {
	my $color;
	my $href = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=Nucleotide&cmd=Search&term=$src&doptcmdl=GenBank";

	if ($pend =~ /5/) {
	    $color = $mred;
	} elsif ($pend =~ /3/) {
	    $color = $mblue;
	} else {
	    if ($stype =~ /mrna/i) {
		$color = $black;
	    } else {
		$color = $lgrey;
	    }
	}
	
	my $glen = length($gaps);

	my $gappedStart = $offset + 1;
	my $gappedEnd = $offset + $glen;

	my $start = $gappedStart - $gapsByPosn->[$gappedStart];
	my $end = $gappedEnd - $gapsByPosn->[($gappedEnd > $gappedSeqLen - 1) ? $gappedSeqLen - 1 : $gappedEnd];

	my $args = {
	    x1 => $start,
	    x2 => $end,
	    height => 3,
	    color => $color,
	    borderColor => $white,
	    filled => 1,
	    border => 1,
	    imagemapHref => $href,
	    imagemapTarget => $hrefTarget,
	};

	my $seqDescr;

	if ($stype =~ /est/i) {
	    if ($pend =~ /[35]/) {
		$seqDescr = "${pend}&#8242; EST";
	    } else {
		$seqDescr = "EST";
	    }
	} else {
	    $seqDescr = $stype;
	}

	my $strandChar = $strand ? '+' : '-';

	if ($self->{javascript}) {
	    my $sh = &safeHtml("$src: $seqLen bp $seqDescr: $defline");
	    my $mOver = "show${fnName}Info('$sh', '$start-$end ($strandChar)', '$src', ''); return true;";
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	my $span = WDK::Model::GDUtil::Span->new($args);
	push(@$spans, $span);
    }	       

    if (scalar(@$spans) > 0) {
	my $ss = WDK::Model::GDUtil::StripeSpan->new({
	    kids => $spans,
	    packer => $lrp2,
	    label => 'ESTs + mRNAs',
	    labelVAlign => 'center',
	});
	return [$ss];
    }
    return [];
}

sub getTranslatedAAFeatureSpans {
    my($self, $canvas) = @_;
    my $spans = [];

    my $sresDb = $self->{sresDb};
    my $dotsDb = $self->{dotsDb};

    my $naSeqId = $self->{'naSeqId'};
    my $dbh = $self->{'dbh'};
    my $hrefTarget = $self->{'hrefTarget'};
    my $name = $self->{name};
    my $fnName = $name;
    $fnName =~ s/[^a-zA-Z0-9]//g;

    my $img = $canvas->getImage();
    my $white = $img->colorClosest(255,255,255);
    my $black = $img->colorClosest(0,0,0);
    my $mblue = $img->colorClosest(100,100,255);
    my $mred = $img->colorClosest(255,100,100);
    my $mgrey = $img->colorClosest(100,100,100);
    my $lgrey = $img->colorClosest(150,150,150);

    my $sql = ("select tafs.translation_score, tafs.start_pos, tafs.end_pos, tafs.aa_start_pos, tafs.aa_end_pos, " .
	       "       taf.diana_atg_score, taf.diana_atg_position, taf.p_value, taf.number_of_segments, " .
	       "       taf.translation_start, taf.translation_stop, taf.translation_score " .
	       "from ${dotsDb}.RNAFeature naf, ${dotsDb}.TranslatedAAFeature taf, " .
	       "     ${dotsDb}.TranslatedAAFeatSeg tafs " .
	       "where naf.na_sequence_id = $naSeqId " .
	       "and naf.na_feature_id = taf.na_feature_id " .
	       "and taf.aa_feature_id = tafs.aa_feature_id " .
	       "order by tafs.aa_start_pos asc " );

    my $sth = $dbh->prepare($sql);
    $sth->execute();
    my $n = 1;

    my $dianaPos = undef;
    my $dianaScore = undef;
    my $transStart = undef;
    my $transStop = undef;
    my $transScore = undef;
    my $numSegs = undef;

    while (my($ts,$start,$end,$aastart,$aaend,$atgScore,$atgPos,$pval,$nsegs,$tstart,$tstop,$tscore) = $sth->fetchrow_array()) {
	my $args = {
	    x1 => $start,
	    x2 => $end,
	    height => 8,
	    color => $mblue,
	    filled => 1,
	    border => 1,
	    borderColor => $white,
	    imagemapHref => "",
	    imagemapTarget => $hrefTarget,
	};
	
	if ($self->{javascript}) {
	    my $sh = &safeHtml("Framefinder segment $n/$nsegs  AA position: $aastart - $aaend");
	    my $mOver = "show${fnName}Info('$sh', '$start-$end', 'segment $n/$nsegs', 'pval=$pval'); return true;";
	    $args->{imagemapOnMouseOver} = $mOver;
	}

	my $span = WDK::Model::GDUtil::Span->new($args);
	push(@$spans, $span);
	++$n;

	$dianaPos = $atgPos;
	$dianaScore = $atgScore;
	$transStart = $tstart;
	$transStop = $tstop;
	$transScore = $tscore;
	$numSegs = $nsegs;
    }

    my $ss1 = WDK::Model::GDUtil::StripeSpan->new({
	kids => $spans,
	packer => $cp0,
	label => 'Framefinder segment(s)',
	labelVAlign => 'bottom',
    });

    # TranslatedAAFeature
    #
    my $tfArgs = {
	x1 => $transStart,
	x2 => $transStop,
	height => 8,
	color => $mblue,
	filled => 1,
	border => 1,
	borderColor => $black,
    };

    my $tfSpan = WDK::Model::GDUtil::Span->new($tfArgs);
    my $tfOver = undef;

    if ($self->{javascript}) {
	my $sh = &safeHtml("Framefinder predicted protein");
	$tfOver = "show${fnName}Info('$sh', '$transStart-$transStop', '$numSegs segment(s)', 'score=$transScore'); return true;";
    }
    
    my $tfSpan = WDK::Model::GDUtil::Span->new($tfArgs);

    my $ss2 = WDK::Model::GDUtil::StripeSpan->new({
	kids => [$tfSpan],
	packer => $cp0,
	label => 'Framefinder protein',
	labelVAlign => 'bottom',
	imagemapArea => 'all',
	imagemapOnMouseOver => $tfOver,
    });

    # DIANA_ATG
    #
    my $dArgs = {
	x1 => $dianaPos,
	x2 => $dianaPos + 2,
	height => 8,
	color => $mred,
	borderColor => $black,
	filled => 1,
	border => 1,
    };

    my $mOver = undef;

    if ($self->{javascript}) {
	my $sh = &safeHtml("ATG site predicted by DIANA");
	$mOver = "show${fnName}Info('$sh', '$dianaPos', '', 'score=$dianaScore'); return true;";
    }

    my $dianaSpan = WDK::Model::GDUtil::Span->new($dArgs);

    my $ss3 = WDK::Model::GDUtil::StripeSpan->new({
	kids => [$dianaSpan],
	packer => $cp0,
	label => 'DIANA_ATG',
	labelVAlign => 'bottom',
	imagemapArea => 'all',
	imagemapOnMouseOver => $mOver,
    });

    return [$ss1, $ss2, $ss3];
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

sub makeSpacerSpan {
    my($height) = @_;

    return WDK::Model::GDUtil::Span->new({
	height => $height,
	shape => 'none',
    });
}
