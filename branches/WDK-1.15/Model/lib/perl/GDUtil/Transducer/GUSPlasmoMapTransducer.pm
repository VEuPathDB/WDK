#!/usr/bin/perl

#------------------------------------------------------------------------
# GUSPlasmoMapTransducer.pm
#
# Displays Plasmodium falciparum optical & microsatellite mapping data
# for a specified chromosome.
#
# Jonathan Crabtree
#
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::Transducer::GUSPlasmoMapTransducer;

use strict;

use DBI;

use WDK::Model::GDUtil::GDCanvas;
use WDK::Model::GDUtil::Span;
use WDK::Model::GDUtil::StripeSpan;
use WDK::Model::GDUtil::AxisSpan;
use WDK::Model::GDUtil::Packer;

#-------------------------------------------------
# Defaults
#-------------------------------------------------

my $DFLT_IMG_WIDTH = 400;
my $DFLT_IMG_HEIGHT = 300;
my $DFLT_JAVASCRIPT = 1;
my $DFLT_AUTO_HEIGHT = 1;
my $DFLT_PROJECT_ID = 8;

#-------------------------------------------------
# Configuration
#-------------------------------------------------

# Hash that records the length of each p. falciparum chromosome
# Used to calibrate the conversion between centimorgans and base pairs
#
my $chromSizesInMB = {
    1 => 0.7,
    2 => 0.95,
    3 => 1.06,
    4 => 1.2,
    5 => 1.4,
    6 => 1.6,
    7 => 1.7,
    8 => 1.7,
    9 => 1.8,
    10 => 2.1,
    11 => 2.4,
    12 => 2.4,
    13 => 3.2,
    14 => 3.4
};

my $chromSeqCenters = {
    1 => 'Sanger',
    2 => 'TIGR/NMRC',
    3 => 'Sanger',
    4 => 'Sanger',
    5 => 'Sanger',
    6 => 'Sanger',
    7 => 'Sanger',
    8 => 'Sanger',
    9 => 'Sanger',
    10 => 'TIGR/NMRC',
    11 => 'TIGR/NMRC',
    12 => 'Stanford',
    13 => 'Sanger',
    14 => 'TIGR/NMRC'
};

#-------------------------------------------------
# GUSSimilarityTransducer
#-------------------------------------------------

sub new {
    my($class, $args) = @_;

    my $iw = $args->{imgWidth};
    my $ih = $args->{imgHeight};
    my $js = $args->{javascript};
    my $autoheight = $args->{autoheight};
    my $projectId = $args->{projectId};

    my $self = {
	width => (defined $iw) ? $iw : $DFLT_IMG_WIDTH,
	height => (defined $ih) ? $ih : $DFLT_IMG_HEIGHT,
	javascript => (defined $js) ? $js : $DFLT_JAVASCRIPT,
	chromosome => $args->{chromosome},
	projectId => (defined $projectId) ? $projectId : $DFLT_PROJECT_ID,
	dbh => $args->{dbh},
	autoheight => (defined $autoheight) ? $autoheight : $DFLT_AUTO_HEIGHT,
	coreDb => $args->{coreDb},
	dotsDb => $args->{dotsDb},
    };
    
    bless $self, $class;
    $self->{name} = $args->{name} if (defined($args->{name}));
    $self->{hrefFn} = $args->{hrefFn} if (defined($args->{hrefFn}));
    $self->{hrefTarget} = $args->{hrefTarget} if (defined($args->{hrefTarget}));
    $self->{seqLabel} = $args->{seqLabel} if (defined($args->{seqLabel}));

    # Microsatellite map
    #
    my $contigRows = $self->getMicrosatelliteMappedContigs();
    my $markerRows = $self->getMicrosatelliteMarkers();
    $self->buildMicrosatelliteMapImage($contigRows, $markerRows);

    # Optical maps (BamHI and NheI)
    #
    my $bamFragments = $self->getOpticalMapFragments('BamHI');
    my $bamAligns = $self->getOpticalMapAlignments('BamHI');

    my $nheFragments = $self->getOpticalMapFragments('NheI');
    my $nheAligns = $self->getOpticalMapAlignments('NheI');

    $self->buildOpticalMapImage('BamHI', $bamFragments, $bamAligns);
    $self->buildOpticalMapImage('NheI', $nheFragments, $nheAligns);

    return $self;
}

sub getHtml {
    my($self, $imgURL1, $imgURL2, $imgURL3, $x1, $x2, $contigList) = @_;
    my $chrom = $self->{chromosome};
    my $name = $self->{name};

    my $html = '';

    if ($self->{javascript}) {
	$html .= "<FORM NAME=\"${name}_form\">\n";
    }

    # Microsatellite map
    #
    $html .= $self->getImageMap($x1, $x2, "microsat");
    $html .= $self->getImageMap($x1, $x2, "BamHI");
    $html .= $self->getImageMap($x1, $x2, "NheI");

    my $w1 = $self->{width};
    my $h1 = $self->{height};

    $html .= "Microsatellite map<BR>\n";
    $html .= "<IMG SRC=\"${imgURL1}\" BORDER=\"1\" WIDTH=\"$w1\" HEIGHT=\"$h1\" ";
    $html .= "USEMAP=\"#microsat_${name}\">\n";

    # Where JavaScript mouseover information is displayed
    #
    if ($self->{javascript}) {
	$html .= "<TABLE BORDER=\"0\" ALIGN=\"center\">\n";
	$html .= "<TR>";

	$html .= "<TD COLSPAN=\"5\">";
	$html .= "<INPUT TYPE=\"text\" NAME=\"microsat_${name}_defline\" SIZE=\"75\"";
	$html .= " VALUE=\"Place the mouse over a sequence or marker to see its description here.\">";
	$html .= "</TD>\n";
	$html .= "<BR CLEAR=\"both\">\n";
	
	$html .= "</TR>";
	$html .= "</TABLE>\n";
    }

    $html .= "<BR CLEAR=\"left\">\n";

    # Optical maps
    #
    $html .= "Optical maps (orientation unknown)<BR>\n";
    my $w2 = $self->{"BamHI_width"};
    my $h2 = $self->{"BamHI_height"};
    $html .= "<IMG SRC=\"${imgURL2}\" BORDER=\"1\" WIDTH=\"$w2\" HEIGHT=\"$h2\" ";
    $html .= "USEMAP=\"#BamHI_${name}\">\n";

    # Where JavaScript mouseover information is displayed
    #
    if ($self->{javascript}) {
	$html .= "<TABLE BORDER=\"0\" ALIGN=\"center\">\n";
	$html .= "<TR>";

	$html .= "<TD COLSPAN=\"5\">";
	$html .= "<INPUT TYPE=\"text\" NAME=\"BamHI_${name}_defline\" SIZE=\"75\"";
	$html .= " VALUE=\"Place the mouse over a sequence or marker to see its description here.\">";
	$html .= "</TD>\n";
	$html .= "<BR CLEAR=\"both\">\n";
	
	$html .= "</TR>";
	$html .= "</TABLE>\n";
    }

    $html .= "<BR CLEAR=\"left\">\n";

    my $w3 = $self->{"NheI_width"};
    my $h3 = $self->{"NheI_height"};
    $html .= "<IMG SRC=\"${imgURL3}\" BORDER=\"1\" WIDTH=\"$w3\" HEIGHT=\"$h3\" ";
    $html .= "USEMAP=\"#NheI_${name}\">\n";

    # Where JavaScript mouseover information is displayed
    #
    if ($self->{javascript}) {
	$html .= "<TABLE BORDER=\"0\" ALIGN=\"center\">\n";
	$html .= "<TR>";

	$html .= "<TD COLSPAN=\"5\">";
	$html .= "<INPUT TYPE=\"text\" NAME=\"NheI_${name}_defline\" SIZE=\"75\"";
	$html .= " VALUE=\"Place the mouse over a sequence or marker to see its description here.\">";
	$html .= "</TD>\n";
	$html .= "<BR CLEAR=\"both\">\n";
	
	$html .= "</TR>";
	$html .= "</TABLE>\n";
    }

    $self->getSeqSummary();

#    print STDERR "GUSPlasmoMapTransducer: totalBp = ", $self->{totalBp}, "\n";
#    print STDERR "GUSPlasmoMapTransducer: mappedBp = ", $self->{mappedBp}, "\n";

    my $totalMb = int($self->{totalBp} / 10000.0 + 0.5) / 100;
    my $mappedMb = int($self->{mappedBp} / 10000.0 + 0.5) / 100;

    $html .= "<BR CLEAR=\"left\">\n";
    $html .= "<FONT SIZE=\"+1\">";
    $html .= "<B>Chr. $chrom";
    $html .= " (" . $chromSizesInMB->{$chrom} . " Mb/" . $self->{rightMarkerCm} . " cM)";
    $html .= "</B><BR> " . $self->{totalSeqs} . " ";
    $html .= "BLOB " if ($chrom =~ /^[678]$/);
    $html .= "sequence(s) available (" . $totalMb . " Mb)";
    $html .= " with ";
    $html .= $self->{mappedSeqs} . " shown " . " (" . $mappedMb . " Mb) that hit a microsatellite";
    $html .= " on chr. $chrom" if ($chrom =~ /^[678]$/);
    $html .= "</FONT>";
    $html .= "<BR>\n";

    if ($self->{javascript}) {
	$html .= "</FORM>\n";
    }

    # Include tabular list of contigs
    # 
    if ($contigList) {
	if ($self->{lastHtml}) {
	    $html .= "<DIV ALIGN=\"center\"><FONT SIZE=\"+1\"><B>e-PCR data:</B></FONT></DIV>\n";

	    $html .= "<TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"2\" ";
	    $html .= "BGCOLOR=\"#ccccff\" ALIGN=\"center\">\n";
	    $html .= "<TR><TD>\n";
	    
	    $html .= "<TABLE BORDER=\"0\" CELLPADDING=\"2\" CELLSPACING=\"1\" ";
	    $html .= "WIDTH=\"100%\" BGCOLOR=\"#ffffff\">\n";

	    $html .= $self->{lastHtml} . "\n";

	    $html .= "</TABLE>\n";

	    $html .= "</TD></TR>\n";
	    $html .= "</TABLE>\n";
	}
    }

    return $html;
}

sub getImageMap {
    my($self, $x1, $x2, $tag) = @_;
    my $name = $self->{name};
    $tag = 'microsat' if (!defined($tag));

    my $im .= "<MAP NAME=\"${tag}_${name}\">\n";
    $im .= $self->{"${tag}_rootSpan"}->makeImageMap($self->{"${tag}_canvas"});
    $im .= "</MAP>\n";

    return $im;
}

sub getPng {
    my($self, $tag) = @_;
    my $img = undef;
    
    if (defined($tag)) {
	$img = $self->{"${tag}_canvas"}->getImage();
    } else {
	$img = $self->{"microsat_canvas"}->getImage();
    }
    return $img->can('png') ? $img->png() : $img->gif();
}

sub getJpeg {
    my($self, $quality, $tag) = @_;

    if (defined($quality)) {
	if (defined($tag)) {
	    return $self->{"${tag}_canvas"}->getImage()->jpeg($quality);
	} else {
	    return $self->{"microsat_canvas"}->getImage()->jpeg($quality);
	}
    }
    if (defined($tag)) {
	return $self->{"${tag}_canvas"}->getImage()->jpeg();
    } else {
	return $self->{"microsat_canvas"}->getImage()->jpeg();
    }
}

# Generate a map image using the microsatellite map data.
#
# Approach #2: 
#
# Using the (approximate) known length of the chromosome along with
# the rightmost marker to convert between centimorgans and base pairs.
#
sub buildMicrosatelliteMapImage {
    my($self, $rows, $markerRows) = @_;

    # "left" and "right" boundaries of the chromosome in centimorgans
    # based on the leftmost and rightmost markers and a correction based
    # on the positions of contigs of known length
    #
    my $adjMinCm = undef;
    my $adjMaxCm = undef;

    my $canvas = 
	WDK::Model::GDUtil::GDCanvas->new($self->{width}, $self->{height},
			      {x1 => 100, x2 => $self->{width} - 30},
			      {x1 => $self->{minCm}, x2 => $self->{maxCm}});

    $canvas->allocateWebSafePalette();
    $self->{"microsat_canvas"} = $canvas;

    my $dfltColor = $canvas->getDefaultFgColor();
    my $uncertColor = $canvas->getImage()->colorExact(153,153,153);

    my $contigs = {};

    foreach my $row (@$rows) {
	my($seqId, $seqSrcId, $len, $cm, $mn, $fm, $x1, $x2) = @$row;
	my $contig = $contigs->{$seqId};

	if (!defined($contig)) {
	    $contig = {
		srcId => $seqSrcId,
		length => $len,
		minCm => $cm,
		maxCm => $cm,
		markers => [{cm => $cm, name => $mn, x1 => $x1, x2 => $x2}],
		};
	    $contigs->{$seqId} = $contig;
	} else {
	    $contig->{minCm} = $cm if ($cm < $contig->{minCm});
	    $contig->{maxCm} = $cm if ($cm > $contig->{maxCm});

	    push(@{$contig->{markers}}, {cm => $cm, name => $mn, x1 => $x1, x2 => $x2});
	}
    }
    
    # Turn contigs into spans; spans/contigs are indexed by source chromosome
    # 
    my $contigSpans = {};

    foreach my $naSeqId (keys %$contigs) {
	my $c = $contigs->{$naSeqId};
	my $contigColor = $dfltColor;

	# If >1 *distinct* marker should be able to determine orientation wrt genetic map
	#
	my $markers = $c->{markers};
	my $nMarkers = scalar(@$markers);

	# Flag to set if the marker information is inconsistent
	#
	my $error = 0;
	my $shape = 'rect';
	my $nc = undef;

	if ($nMarkers > 1) {
	    my @sorted = sort { $a->{cm} <=> $b->{cm} } @$markers;  # sort by cM

	    # Collapse adjacent markers
	    #
	    my $collapsed = [];
	    my $lastM = undef;

	    foreach my $m (@sorted) {
		my $cm = $m->{cm};

		if (defined($lastM) && ($lastM->{cm} == $cm)) {
		    $lastM->{x1} = $lastM->{x1} < $m->{x1} ? $lastM->{x1} : $m->{x1};
		    $lastM->{x2} = $lastM->{x2} < $m->{x2} ? $lastM->{x2} : $m->{x2};
		} else {
		    my $new = {cm => $m->{cm}, name => $m->{name}, x1 => $m->{x1}, x2 => $m->{x2}};
		    push(@$collapsed, $new);
		    $lastM = $new;
		}
	    }

	    $nc = scalar(@$collapsed);
	    my $forward = undef;

	    if ($nc > 1) {
		my $lastcoord = 0;

		my $first = $collapsed->[0];
		my $second = $collapsed->[1];

		$forward = ($first->{x1} < $second->{x2});
		
		# Check that the remaining markers agree with the order
		# established by the first two.
		#
		my $lastCoord = $second->{x1};
		my $lastCm = $second->{cm};
		
		for (my $i = 2;$i < $nc;++$i) {
		    my $m = $collapsed->[$i];
		    my $coord = $m->{x1};
		    my $cm = $m->{cm};
		    
		    # It's only an error if the genetic location is different
		    # from the last.
		    #
		    if ($cm > $lastCm) {
			if ($forward) {
			    $error = 1 if ($coord < $lastCoord);
			} else {
			    $error = 1 if ($coord > $lastCoord);
			}
		    }
		    $lastCoord = $coord;
		    $lastCm = $cm;
		}

		# Set shape based on orientation
		#
		$shape = $forward ? 'forward' : 'reverse' if (!$error);
	    }

#	    if ($error) {
#		print STDERR "GUSPlasmoMapTransducer: naSeqId=$naSeqId num markers=$nMarkers error=$error\n";
#		print STDERR "GUSPlasmoMapTransducer: ";
#		foreach my $s (@sorted) {
#		    print STDERR "[cm=", $s->{cm}, " x1=", $s->{x1}, " x2=", $s->{x2}, " name=", $s->{name}, "] ";
#		}
#		print STDERR "\n";
#	    }
	}
	
	# Determine amount of sequence in bp to the left and right of
	# the leftmost and rightmost markers, respectively
	#
	if ($nMarkers >= 1) {
	    my $leftM = $markers->[0];
	    my $rightM = $markers->[$nMarkers - 1];

#	    print STDERR "GUSPlasmoMapTransducer.pm: shape = $shape\n";
	    
	    if ($shape eq 'reverse') {
		$c->{leftOverhang} = $c->{length} - $leftM->{x2} + 1;
		$c->{rightOverhang} = $rightM->{x2};
	    } elsif ($shape eq 'forward') {
		$c->{leftOverhang} = $leftM->{x1};
		$c->{rightOverhang} = $c->{length} - $rightM->{x2} + 1;
	    } else {
		my $lo1 = $leftM->{x1};
		my $lo2 = $c->{length} - $leftM->{x2} + 1;
		my $ro1 = $rightM->{x2};
		my $ro2 = $c->{length} - $rightM->{x2} + 1;

		$c->{leftOverhang} = ($lo1 < $lo2) ? $lo1 : $lo2;
		$c->{rightOverhang} = ($ro1 < $ro2) ? $ro1 : $ro2;
	    }

#	    foreach my $m (@$markers) {
#		print STDERR "[cm=", $m->{cm}, " x1=", $m->{x1}, " x2=", $m->{x2}, "] ";
#	    }
#	    print STDERR "\n";

#	    print STDERR "GUSPlasmoMapTransducer: ",
#	    " leftM->cm ", $leftM->{cm},
#	    " rightM->cm ", $rightM->{cm},
#	    " leftM->x1 ", $leftM->{x1},
#	    " rightM->x2 ", $rightM->{x2}, "\n";

#	    print STDERR "GUSPlasmoMapTransducer: ", $c->{srcId},
#	    " shape = ", $shape,
#	    " left overhang = ", $c->{leftOverhang}, 
#	    " right overhang = ", $c->{rightOverhang},"\n"

	    # If we don't know the orientation, enlarge the span
	    # to reflect its possible positions and change its color.
	    #
	    if (0 && (($nMarkers) == 1 || ($nc == 1))) {
		my $lo = $c->{leftOverhang};
		my $ro = $c->{rightOverhang};
		$c->{uncertainty} = $lo > $ro ? $lo : $ro;
		$c->{leftOverhang} = $c->{uncertainty};
		$c->{rightOverhang} = $c->{uncertainty};
		$contigColor = $uncertColor;
	    }
	    
	}

	# Adjust coordinates based on overhangs (in bp) and conversion
	# from bp -> centimorgans
	#
#	print STDERR "left overhang = ", $c->{leftOverhang}, " right overhang = ", $c->{rightOverhang}, "\n";

	my $leftOverhangCm = $self->bpToCm($c->{leftOverhang});
	my $rightOverhangCm = $self->bpToCm($c->{rightOverhang});

	my $x1 = $c->{minCm} - $leftOverhangCm;
	my $x2 = $c->{maxCm} + $rightOverhangCm;

	# Adjusted boundaries for the axis computed using the
	# bp -> cm conversion.
	#
	if (!defined($adjMinCm)) {
	    $adjMinCm = $x1;
	    $adjMaxCm = $x2;
	} else {
	    $adjMinCm = $x1 if ($x1 < $adjMinCm);
	    $adjMaxCm = $x2 if ($x2 > $adjMaxCm);
	}

	my $descr = ($c->{srcId} . " [" . $c->{length} . 
		     "bp num_markers=" . scalar(@{$c->{markers}}) .
		     " minCm=" . int($x1 + .5) . " maxCm=" . int($x2 + .5) . "]");

	my $hrefFn = $self->{hrefFn};
	my $hrefTarget = $self->{hrefTarget};
	my $name = $self->{name};
	$descr = &safeHtml($descr);
	
	my $mOver = 
	    ("window.status = '$descr';" .
	     "document.forms['${name}_form']['microsat_${name}_defline'].value='$descr';" .
	     "return true;");

	# Add kids to represent the markers
	#
	my $mSpans;
	my $mcolor = $self->{"microsat_canvas"}->getImage()->colorExact(255,0,0);

	foreach my $m (@$markers) {
	    my($mx1, $mx2) = $self->fudgeCoordinates($m->{cm}, $m->{cm});
	    $mx1 = $x1 if ($mx1 < $x1);	    
	    $mx2 = $x2 if ($mx2 > $x2);

	    push(@$mSpans, WDK::Model::GDUtil::Span->new({
		x1 => $mx1,
		x2 => $mx2,
		height => 3,
		vertOffset => -2,
		color => $mcolor,
		imagemapHref => defined($hrefFn) ? &$hrefFn($naSeqId) : undef,
		imagemapTarget => defined($hrefTarget) ? $hrefTarget : undef,
		imagemapOnMouseOver => $self->{javascript} ? $mOver : undef,
	    }));
	}

	my $contigSpan = WDK::Model::GDUtil::Span->new({
	    x1 => $x1,
	    x2 => $x2,
	    height => 7,
	    imagemapLabel => $descr,
	    packer => &WDK::Model::GDUtil::Packer::constantPacker(0),
	    shape => $shape,
	    color => $contigColor, 
	    filled => !$error,
	    imagemapHref => defined($hrefFn) ? &$hrefFn($naSeqId) : undef,
	    imagemapTarget => defined($hrefTarget) ? $hrefTarget : undef,
	    imagemapOnMouseOver => $self->{javascript} ? $mOver : undef,
	});

	unshift(@$mSpans, $contigSpan);

	my $span = WDK::Model::GDUtil::Span->new({
	    x1 => $x1,
	    x2 => $x2,
	    height => 0,
	    kids => $mSpans,
	    packer => &WDK::Model::GDUtil::Packer::constantPacker(0),
	    shape => 'none',
	    
	});

	# Some attributes of the contig to duplicate in the span
	#
	$span->{'_num_markers'} = scalar(@{$c->{markers}});
	$span->{'_length'} = $c->{length};

#	print STDERR "GUSPlasmoMapTransducer: num markers = ", scalar(@{$c->{markers}}), "\n";

	my $sourceChrom;

	if ($c->{srcId} =~ /NC\_000910/) {
	    $sourceChrom = '2';
	} elsif ($c->{srcId} =~ /NC\_000521/) {
	    $sourceChrom = '3';
	} else {
	    ($sourceChrom) = $c->{srcId} =~ /chr(\d+|BLOB)[^\d]/;
	}

	my $cspans = $contigSpans->{$sourceChrom};
	if (!defined($cspans)) {
	    $cspans = [];
	    $contigSpans->{$sourceChrom} = $cspans;
	}
	push(@$cspans, $span);
    }

    my $stripeSpans = [];
    my $markerSpans = [];

    # The markers themselves
    #
    my $mcolor = $self->{"microsat_canvas"}->getImage()->colorExact(255,0,0);
    my $name = $self->{name};
    
    foreach my $mrow (@$markerRows) {
	my($srcId, $mname, $synonym, $accn, $cm, $framework, $cross) = @$mrow;

	my($x1, $x2) = $self->fudgeCoordinates($cm, $cm);
	my $descr = &safeHtml("Marker $mname accn=$accn map=${cm}cM $framework cross=$cross");
			
	my $mOver = 
	    ("window.status = '$descr';" .
	     "document.forms['${name}_form']['microsat_${name}_defline'].value='$descr';" .
	     "return true;");

	push(@$markerSpans, WDK::Model::GDUtil::Span->new({
	    x1 => $x1,
	    x2 => $x2,
	    height => 8,
	    imagemapLabel => $descr,
	    imagemapHref => '#',
	    imagemapOnMouseOver => $self->{javascript} ? $mOver : undef,
	    color => $mcolor
	}));
    }
    push(@$stripeSpans, &makeStripeSpan("Markers", $markerSpans, &WDK::Model::GDUtil::Packer::constantPacker(0)));

    # Group contigs by source chromosome, putting those for _this_ chromosome
    # closest to the axis.  Then sort by number of markers (those with more
    # are closer to the axis.)
    #
    my $byNumMarkers = sub {
	return (($b->{'_num_markers'} <=> $a->{'_num_markers'}) 
		|| $b->{'_length'} <=> $a->{'_length'});
    };

    my $selfContigs = $contigSpans->{$self->{chromosome}};

    # Contigs on _this_ chromosome
    #
    if (defined($selfContigs)) {
	my @sortedCs = sort $byNumMarkers @$selfContigs;
	push(@$stripeSpans, &makeStripeSpan("Chr. " . $self->{chromosome}, \@sortedCs));
    }

    # All other contigs
    #
    foreach my $c (sort {$a <=> $b} keys %$contigSpans) {
	next if ($c eq $self->{chromosome});
	my @sortedCs = sort $byNumMarkers @{$contigSpans->{$c}};
	push(@$stripeSpans, &makeStripeSpan("Chr. $c", \@sortedCs));
    }

    my $oldMaxCm = $self->{maxCm};
    $self->{rightMarkerCm} = $oldMaxCm;
    $self->{minCm} = $adjMinCm if ($adjMinCm < $self->{minCm});
    $self->{maxCm} = $adjMaxCm if ($adjMaxCm > $self->{maxCm});

    $self->{microsat_rootSpan} = 
      WDK::Model::GDUtil::AxisSpan->new({
	  x1 => $self->{minCm}, 
	  x2 => $self->{maxCm}, 
	  y1 => $self->{height} - 5, 
	  height => 6, tickHeight => 4, tickWidth => 1,
	  kids => $stripeSpans,
	  packer => WDK::Model::GDUtil::Packer::simplePacker(5),
	  ticks => [0, $oldMaxCm],
	  tickLabel => 'cM',
	  label => $self->{seqLabel},
	  labelVAlign => 'bottom',
      });
    
    $self->{microsat_rootSpan}->pack();

    # Ugly, but the only way
    #
    if ($self->{autoheight}) {
	$self->{height} = $self->{microsat_rootSpan}->getHeight() + 5;

	my $canvas = 
	  WDK::Model::GDUtil::GDCanvas->new($self->{width}, $self->{height},
				{x1 => 100, x2 => $self->{width} - 30},
				{x1 => $self->{minCm}, x2 => $self->{maxCm}});

	$canvas->allocateWebSafePalette();
	$self->{"microsat_canvas"} = $canvas;
	$self->{microsat_rootSpan}->{y1} = $self->{height} - 5;
	$self->{microsat_rootSpan}->pack();
    }

    $self->{microsat_rootSpan}->draw($self->{"microsat_canvas"});
}

# Get optical map fragments for a restriction enzyme.
#
sub getOpticalMapFragments {
    my($self, $enzyme) = @_;
    my $dotsDb = $self->{dotsDb};
    
    my $rows = [];
    my $chrom = $self->{chromosome};
    
    my $sql = ("select om.restriction_enzyme, om.chromosome_orientation, " .
	       "       omf.fragment_length_kb, omf.cumulative_length_kb " .
	       "from ${dotsDb}.opticalmap om, ${dotsDb}.opticalmapfragment omf " .
	       "where (om.chromosome = 'chr$chrom' or om.chromosome = 'blob$chrom') " .
	       "and om.restriction_enzyme = '$enzyme' " .
	       "and om.taxon_id = 211 " .
	       "and omf.optical_map_id = om.optical_map_id " .
	       "order by om.restriction_enzyme, omf.fragment_order asc ");
    
    my $sth = $self->{dbh}->prepare($sql); 
    $sth->execute();

    while (my @row = $sth->fetchrow_array()) { 
	push(@$rows, \@row);
    }

    $sth->finish();
    return $rows;
}

sub getOpticalMapAlignments {
    my($self, $enzyme) = @_;

    my $dotsDb = $self->{dotsDb};
    
    my $rows = [];
    my $chrom = $self->{chromosome};
    my $projectId = $self->{projectId};

    # TO DO - restrict to a particular mapping (only 1 so far, alg_id = 2390)
    
    my $sql = ("select align.optical_map_alignment_id, align.na_sequence_id, align.score, " .
	       "       align.p_value, align.start_fragment, align.end_fragment, " .
	       "       ena.source_id, ena.length, ena.chromosome " .
	       "from ${dotsDb}.opticalmap om, ${dotsDb}.opticalmapalignment align, " .
	       "     ${dotsDb}.externalnasequence ena, ${dotsDb}.projectlink pl " .
	       "where (om.chromosome = 'chr$chrom' or om.chromosome = 'blob$chrom') " .
	       "and om.restriction_enzyme = '$enzyme' " .
	       "and om.taxon_id = 211 " .
	       "and om.optical_map_id = align.optical_map_id " .
	       "and align.na_sequence_id = ena.na_sequence_id " .
	       "and align.score >= 100 " .
	       "and align.end_fragment - align.start_fragment > 3 " .
	       "and pl.project_id = $projectId " .
	       "and pl.table_id = 89 " .
	       "and pl.id = ena.na_sequence_id " .
	       "order by align.optical_map_alignment_id ");
    
    my $sth = $self->{dbh}->prepare($sql); 
    $sth->execute();

    while (my @row = $sth->fetchrow_array()) { 
	push(@$rows, \@row);
    }

    $sth->finish();
    return $rows;
}

# Build a map image using the optical map data and one or more
# contig -> chromosome mappings using the data.
#
sub buildOpticalMapImage {
    my($self, $enzyme, $optMapFragments, $optMapAlignments) = @_;

    my $nFrags = scalar(@$optMapFragments);
    my $mapLen = $optMapFragments->[$nFrags-1]->[3];
    $self->{"${enzyme}_width"} = $self->{width};
    $self->{"${enzyme}_height"} = $self->{height};

    my $canvas = 
	WDK::Model::GDUtil::GDCanvas->new($self->{"${enzyme}_width"}, $self->{"${enzyme}_height"},
			      {x1 => 100, x2 => $self->{"${enzyme}_width"} - 30},
			      {x1 => 0, x2 => $mapLen});

    $canvas->allocateWebSafePalette();

    my $dfltColor = $canvas->getDefaultFgColor();
    my $grey = $canvas->getImage()->colorExact(102,102,102);
    my $black = $canvas->getImage()->colorExact(0,0,0);
    my $red = $canvas->getImage()->colorExact(255,0,0);

    $self->{"${enzyme}_canvas"} = $canvas;
    my $fragSpans = [];
    my $fragPosns = [];

    # A horizontal span showing the composite optical map digest
    #
    my $first = 1;

#    push(@$fragSpans, WDK::Model::GDUtil::Span->new({
#	x1 => 1,
#	x2 => $mapLen,
#	height => 8,
#	color => $grey,
#	filled => 0
#	}));

    foreach my $frag (@$optMapFragments) {
	my $start = $frag->[3] - $frag->[2];
	my $end = $frag->[3];
	push(@$fragPosns, {'start' => $start, 'end' => $end});

	push(@$fragSpans, WDK::Model::GDUtil::Span->new({
	    x1 => $start,
	    x2 => $end,
	    height => 8,
	    color => $red,
	    filled => 0,
	    }));
    }

    my $optMapSpan = WDK::Model::GDUtil::Span->new({
	x1 => 0,
	x2 => $mapLen,
	packer => &WDK::Model::GDUtil::Packer::constantPacker(0),
	kids => $fragSpans,
	height => 0,
	shape => 'none'
    });

    # Spans representing the genomic sequence contigs aligned with the optical 
    # map, indexed by source chromosome.
    #
    my $chromSpans = {};

    my $hrefFn = $self->{hrefFn};
    my $hrefTarget = $self->{hrefTarget};

    foreach my $align (@$optMapAlignments) {
	my($alignId, $naSeqId, $score, $pval, $start_frag, $end_frag, $srcId, $len, $chrom) = @$align;
	
	# Calculate sequence coordinate based on fragment positions 
	#
	my $start = $fragPosns->[$start_frag - 1]->{'start'};
	my $end = $fragPosns->[$end_frag - 1]->{'end'};
	my $descr = &safeHtml("$srcId [${len} bp score=$score locn=${start_frag}-${end_frag}]");
	my $name = $self->{name};

	my $mOver = 
	    ("window.status = '$descr';" .
	     "document.forms['${name}_form']['${enzyme}_${name}_defline'].value='$descr';" .
	     "return true;");

	my $sl = $chromSpans->{$chrom};

	if (!defined($sl)) {
	    $sl = [];
	    $chromSpans->{$chrom} = $sl;
	}

	push(@$sl, WDK::Model::GDUtil::Span->new({
	    x1 => $start,
	    x2 => $end,
	    height => 6,
	    color => $black,
	    imagemapHref => defined($hrefFn) ? &$hrefFn($naSeqId) : undef,
	    imagemapTarget => defined($hrefTarget) ? $hrefTarget : undef,
	    imagemapOnMouseOver => $self->{javascript} ? $mOver : undef
	}));
    }

    my $selfContigs = $chromSpans->{$self->{chromosome}};
    my $stripeSpans = [];

    # Contigs on _this_ chromosome
    #
    if (defined($selfContigs)) {
	push(@$stripeSpans, &makeStripeSpan("Chr. " . $self->{chromosome}, $selfContigs));
    }

    # All other contigs
    #
    foreach my $c (sort {$a <=> $b} keys %$chromSpans) {
	next if ($c eq $self->{chromosome});
	push(@$stripeSpans, &makeStripeSpan("Chr. $c", $chromSpans->{$c}));
    }

    $self->{"${enzyme}_rootSpan"} = 
      WDK::Model::GDUtil::AxisSpan->new({
	  x1 => 0, 
	  x2 => $mapLen, 
	  y1 => 150, 
	  height => 6, tickHeight => 4, tickWidth => 1,
	  kids => [&makeStripeSpan($enzyme . " (" . scalar(@$fragPosns) . ")", [$optMapSpan]), @$stripeSpans],
	  packer => WDK::Model::GDUtil::Packer::simplePacker(5),
	  ticks => [0, $mapLen],
	  tickLabel => 'kb',
	  label => $self->{seqLabel},
	  labelVAlign => 'bottom',
      });
    
    $self->{"${enzyme}_rootSpan"}->pack();

    # Ugly, but the only way
    #
    if ($self->{autoheight}) {
	$self->{"${enzyme}_height"} = $self->{"${enzyme}_rootSpan"}->getHeight() + 5;

	my $canvas = 
	  WDK::Model::GDUtil::GDCanvas->new($self->{"${enzyme}_width"}, $self->{"${enzyme}_height"},
				{x1 => 100, x2 => $self->{"${enzyme}_width"} - 30},
				{x1 => 0, x2 => $mapLen});

	$canvas->allocateWebSafePalette();
	$self->{"${enzyme}_canvas"} = $canvas;
	$self->{"${enzyme}_rootSpan"}->{y1} = $self->{"${enzyme}_height"} - 5;
	$self->{"${enzyme}_rootSpan"}->pack();
    }

    $self->{"${enzyme}_rootSpan"}->draw($self->{"${enzyme}_canvas"});
}

my $sPacker2 = &WDK::Model::GDUtil::Packer::simplePacker(2);

# Create a StripeSpan containing all the contigs for a given 
# chromosome.
#
sub makeStripeSpan {
    my($label, $contigs, $packer) = @_;

    return WDK::Model::GDUtil::StripeSpan->new({
	kids => $contigs,
	packer => defined($packer) ? $packer : $sPacker2,
	label => $label,
	labelVAlign => 'top',
	drawBar => 1
    });
}

# Query the mapping table for a set of contigs ordered and
# oriented along the chromosome.
#
sub getMicrosatelliteMappedContigs {
    my($self) = @_;
    my $dotsDb = $self->{dotsDb};

    my $tableHtml = ("<TR>" . 
		     "<TH><FONT FACE=\"sans-serif\">Sequence</FONT></TH>" .
		     "<TH><FONT FACE=\"sans-serif\">marker</FONT></TH>" .
		     "<TH><FONT FACE=\"sans-serif\">centimorgans</FONT></TH>" .
		     "<TH><FONT FACE=\"sans-serif\">score</FONT></TH>" .
		     "<TH><FONT FACE=\"sans-serif\">epcr_start</FONT></TH>" .
		     "<TH><FONT FACE=\"sans-serif\">epcr_end</FONT></TH>" .
		     "<TH><FONT FACE=\"sans-serif\">sequence length</FONT></TH>" .
		     "</TR>\n");
    
    my $chrom = $self->{chromosome};
    my $projectId = $self->{projectId};
    my $hrefFn = $self->{hrefFn};

    my $rows = [];

    my $csql = ("select s.na_sequence_id, s.source_id, s.length, " .
		"       pm.centimorgans, pm.marker_name, pm.framework, " .
		"       e.start_pos, e.stop_pos " .
		"from ${dotsDb}.plasmomap pm, ${dotsDb}.epcr e, ${dotsDb}.externalnasequence s, " .
		"     ${dotsDb}.projectlink pl " .
		"where e.map_table_id = 471 " .
		"      and e.map_id = pm.plasmomap_id " .
		"      and s.na_sequence_id = e.na_sequence_id " .
		"      and s.na_sequence_id = pl.id " .
		"      and pl.table_id = 89 " .
		"      and pl.project_id = $projectId " .
		"      and pm.chromosome = $chrom " .
		"      and pm.centimorgans is not null " .
		"order by pm.centimorgans asc, s.na_sequence_id, e.start_pos asc");
    
    my $sth = $self->{dbh}->prepare($csql); 
    $sth->execute();

    while (my @row = $sth->fetchrow_array()) { 
	push(@$rows, \@row);
	my($id, $srcId, $len, $cm, $mn, $fm, $x1, $x2) = @row;
#	print STDERR "PlasmoMap: id=$id srcId=$srcId len=$len cm=$cm mn=$mn x1=$x1 x2=$x2\n";

	$tableHtml .= "<TR>";

	if (defined($hrefFn)) {
	    my $tgt = defined($self->{hrefTarget}) ? 
		"TARGET=\"" . $self->{hrefTarget} . "\" " : "";
	    $tableHtml .= "<TD><A ${tgt}HREF=\"" . &$hrefFn($id) . "\">$srcId</A></TD>";
	} else {
	    $tableHtml .= "<TD>$srcId</TD>";
	}

	$tableHtml .= "<TD ALIGN=\"center\">$mn</TD>";
	$tableHtml .= "<TD ALIGN=\"center\">$cm</TD>";
	$tableHtml .= "<TD ALIGN=\"center\">$fm</TD>";
	$tableHtml .= "<TD ALIGN=\"center\">$x1</TD>";
	$tableHtml .= "<TD ALIGN=\"center\">$x2</TD>";
	$tableHtml .= "<TD ALIGN=\"center\">$len</TD>";

	$tableHtml .= "</TR>";
    }

    $self->{lastHtml} = $tableHtml;
    $sth->finish();
    return $rows;
}

# Query the mapping table for the markers
#
sub getMicrosatelliteMarkers {
    my($self) = @_;
    my $dotsDb = $self->{dotsDb};

    my $chrom = $self->{chromosome};
    my $rows = [];

    my $csql = ("select pm.source_id, pm.marker_name, pm.marker_synonym, " .
		"pm.accession, pm.centimorgans, pm.framework, pm.cross " .
		"from ${dotsDb}.plasmomap pm " .
		"      where pm.chromosome = $chrom " .
		"      and pm.centimorgans is not null " .
		"order by pm.centimorgans asc ");
    
    my $sth = $self->{dbh}->prepare($csql); 
    $sth->execute();

    my $rows = [];

    while (my @row = $sth->fetchrow_array()) { 
	my($srcId, $name, $synonym, $accn, $cm, $framework, $cross) = @row;
	push(@$rows, \@row);

	if (defined($self->{minCm})) {
	    $self->{minCm} = $cm if ($cm < $self->{minCm});;
	    $self->{maxCm} = $cm if ($cm > $self->{maxCm});;
	} else {
	    $self->{minCm} = $cm;
	    $self->{maxCm} = $cm;
	}
    }

    $self->{minCm} = 0;
    $sth->finish();
    return $rows;
}

# Summarize the number of contigs & bp mapped using the microsatellites
# and not mapped using the microsatellites
#
sub getSeqSummary {
    my($self) = @_;
    my $coreDb = $self->{coreDb};
    my $dotsDb = $self->{dotsDb};

    my $chrom = $self->{chromosome};
    my $projectId = $self->{projectId};

    my $regex = $chrom =~ /^6|7|8$/ ? "chrBLOB\\_%" : "chr${chrom}\\_%";

    $regex = 'NC_000910' if ($chrom == 2);
    $regex = 'NC_000521' if ($chrom == 3);

    # Count total number of contigs/sequences and bp on this chromosome.
    #
    my $sql1 = ("select count(s.na_sequence_id), sum(s.length) " .
		"from ${dotsDb}.externalnasequence s, ${dotsDb}.projectlink pl " .
		"where s.external_database_release_id in (150,151,692) " .
		"and s.source_id like '$regex' ESCAPE '\\' " .
		"and s.na_sequence_id = pl.id " .
		"and pl.table_id = 89 " .
		"and pl.project_id = $projectId ");

#    print STDERR "GUSPlasmoMapTransducer: running '$sql1'\n";

    my $sth = $self->{dbh}->prepare($sql1); 
    $sth->execute();
    while (my @row = $sth->fetchrow_array()) { 
	($self->{totalSeqs}, $self->{totalBp}) = @row;
    }
    $sth->finish();

    # Count the numbers for sequences mapped through the markers
    # require pm.centimorgans to be not null?
    #
    my $regex = $chrom =~ /^6|7|8$/ ? "chrBLOB\\_%" : "chr${chrom}\\_%";

    $regex = 'NC_000910' if ($chrom == 2);
    $regex = 'NC_000521' if ($chrom == 3);

    my $sql2 = ("select count(na_sequence_id), sum(length) from ( " .
		"select distinct s.na_sequence_id, s.length " .
		"from ${dotsDb}.plasmomap pm, ${dotsDb}.epcr e, ${dotsDb}.externalnasequence s, " .
		"     ${coreDb}.tableinfo ti, ${dotsDb}.projectlink pl " .
		"where pm.chromosome = $chrom " .
		"      and ti.name = 'PlasmoMap' " .
		"      and e.map_table_id = ti.table_id " .
		"      and e.map_id = pm.plasmomap_id " .
		"      and e.na_sequence_id = s.na_sequence_id " .
		"      and s.na_sequence_id = pl.id " .
		"      and pl.table_id = 89 " .
		"      and pl.project_id = $projectId " .
		"      and s.source_id like '$regex' ESCAPE '\\' " .
		"      and pm.centimorgans is not null )"); 

#    print STDERR "GUSPlasmoMapTransducer: running '$sql2'\n";

    my $sth = $self->{dbh}->prepare($sql2); 
    $sth->execute();
    while (my @row = $sth->fetchrow_array()) { 
	($self->{mappedSeqs}, $self->{mappedBp}) = @row;
    }
    $sth->finish();
}

# Convert (a quantity) in base pairs to centimorgans
#
sub bpToCm {
    my($self, $bp) = @_;

    my $chrom = $self->{chromosome};
    my $maxCm = $self->{maxCm};
    my $maxBp = $chromSizesInMB->{$chrom} * 1000 * 1000;

#    print STDERR "GUSPlasmoMapTransducer: chrom=$chrom bp=$bp maxBp=$maxBp  maxCm=$maxCm\n";

    my $bpPerCm = $maxBp / $maxCm;
    my $cm = $bp / $bpPerCm;

#    print STDERR "GUSPlasmoMapTransducer: converted $bp -> $cm\n";

    return $cm;
}

# Artificially adjust endpoints to make things show up.
#
sub fudgeCoordinates {
    my($self, $a, $b) = @_;

    my $x1 = $a;
    my $x2 = $b;

    if ($x1 == $self->{minCm}) {
	$x2 += .5;
    } elsif ($x2 == $self->{maxCm}) {
	$x1 -= .5;
    } else {
	$x1 -= .25;
	$x2 += .25;
    }

    return ($x1, $x2);
}

sub safeHtml {
    my($str) = @_;
    $str =~ s#<#&lt;#g;
    $str =~ s#>#&gt;#g;
    $str =~ s#'##g;
    return $str;
}
