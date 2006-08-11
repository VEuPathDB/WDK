#!/usr/bin/perl

#------------------------------------------------------------------------
# ExpressionGraph.pm
# 
# Draw a graph of an expression time course in RAD3.  Currently hard-
# coded to work with the DeRisi MOS12 Plasmodium falciparum HB3 time 
# course data, but most aspects of the script are meant to be generalized.  
# The main problem in doing so is the lack of structured study information 
# in the database.  For example, a mechanism exists to group assays into a 
# study, but there is no guarantee of a similar grouping for the 
# acquisitions, quantifications, and normalization steps that are 
# subsequently peformed on those assays.
#
# TO DO - decide what to do with genes that have multiple composite 
# elements (i.e. just average the values or display overlaid plots?)
#
# Created: Fri Feb  7 01:08:08 EST 2003
#
# Jonathan Crabtree
#
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::Transducer::ExpressionGraph;

use strict;
use GD;

my $DEFAULT_MARGINS = { top => 5, left => 5, bottom => 5, right => 30 };
my $DEFAULT_SQUARE_SIZE = 2;

# Constructor
# 
# $args
#  width           width of the image in pixels
#  height          height of the image in pixels
#  dbh             DBI database handle with select permissions on RAD3
#  normalization   Hack - either 'new' or 'old'
#
sub new {
    my($class, $args) = @_;

    my $image = new GD::Image($args->{width}, $args->{height});
    my $white = $image->colorAllocate(255,255,255);
    my $black = $image->colorAllocate(0,0,0);

    my $self = {
	image => $image,
	width => $args->{width},
	height => $args->{height},
	fg_color => $black,
	bg_color => $white,
	dbh => $args->{dbh},
	coreDb => $args->{coreDb},
	sresDb => $args->{sresDb},
	dotsDb => $args->{dotsDb},
	radDb => $args->{radDb},
	normalization => ($args->{normalization} =~ /old/i) ? 'old' : 'new', # new is default
	square_size => ($args->{square_size} =~ /\d/) ? $args->{square_size} : $DEFAULT_SQUARE_SIZE,
	noYAxisLabel => $args->{noYAxisLabel},
	noCaption => $args->{noCaption},
	noSquares => $args->{noSquares},
	shadeColor => ($args->{shadeColor} =~ /\d+,\d+,\d+/) ? $args->{shadeColor} : '204,204,204',
	lineColor => ($args->{lineColor} =~ /\d+,\d+,\d+/) ? $args->{lineColor} : '0,0,0',
	showSmoothedPlot => $args->{showSmoothedPlot},
	valueShiftAmount => $args->{valueShiftAmount},
	shiftAmount => $args->{shiftAmount},
	minTime => $args->{minTime},
	maxTime => $args->{maxTime},
	};

    bless $self, $class;
    $self->allocateWebSafePalette();
    return $self;
}

# TO DO - investigate what's going on with new GD; colorClosest
# fails to return the exact color requested when it is available
# in the palette.

# Try to allocate the 216 colors deemed "safe" for use on the Web.
# Will only work correctly if there are 216 open slots.
#
sub allocateWebSafePalette {
    my $self = shift;
    my $image = $self->{image};
    my $cVals = [0, 51, 102, 153, 204, 255];

    for my $r (@$cVals) {
	for my $g (@$cVals) {
	    for my $b (@$cVals) {
		my $index = $image->colorAllocate($r, $g, $b);
	    }
	}
    }
}

# Check whether the gene in question has expression data to plot.
#
sub hasData {
    my($self, $type, $studyName, $geneFeatureSrcId, $projectId) = @_;

    my($studyId, $studyName) = $self->getStudy($studyName);
    my $assays = $self->getStudyAssays($studyId);
    my @arrayIds = map {$_->{array_id}} @$assays;
    my $compElts = $self->getGeneCompositeElements($geneFeatureSrcId, $projectId, \@arrayIds);

    return (scalar(@$compElts) > 0);
}

# Generate dataset from expression values passed directly to the method.
#
sub getDataFromInput {
    my($self, $geneDescr, $times, $values) = @_;

    my $valueShiftAmount = $self->{valueShiftAmount};

    # Get some colors and fonts
    # 
    my $image = $self->{image};
    my $red = $image->colorExact(255,51,51);

    my $data = [];
    my $nt = scalar(@$times);

    for (my $i = 0;$i < $nt;++$i) {
	push(@$data, { time => $times->[$i], value => $values->[$i] });
    }

    $data = $self->shiftTimeCourseData($data, $valueShiftAmount) if (defined($valueShiftAmount));

    my $plot = { 
	descr => "red plot: $geneDescr",
	htmlDescr => "<B>red plot</B>: $geneDescr",
	color => $red,
	data => $data
	};

    return [ $plot ];
}

# Draw the graph on the internal GD $image, based on expression values
# passed directly to the method.  This call is usually followed by a call 
# to getPng or getJpeg to retrieve the actual image data.
#
# $times    arrayref of time points
# $values   arrayref of expression values (or pseudo-values or whatever)
#
sub drawGraphFromInput {
    my($self, $type, $studyName, $geneDescr, $times, $values, $projectId, $xAxisLabel, $minY, $maxY, 
       $drawAreaUnderCurve, $drawHorizLines) = @_;

    my $image = $self->{image};
    my $black = $image->colorExact(0,0,0);
    my $blue = $image->colorExact(0,0,255);

    my $normalizedData = ($type =~ /norm/i);
    my $relativeValues = ($type =~ /^rel/i);
    my $bothChannels = 0;

    my $tcData = $self->getDataFromInput($geneDescr, $times, $values);
    my $xparams = $self->getXAxisParams($tcData);
    my $yparams = $self->getYAxisParams($tcData, $normalizedData, $relativeValues, 0, $minY, $maxY);

    my $margins = $DEFAULT_MARGINS;

    # Display caption at the top describing the gene / study
    my $fonth = gdSmallFont->height;

    if (!$self->{noCaption}) {
	$image->string(gdSmallFont, $margins->{left}, $margins->{top}, $geneDescr, $black);
	$image->string(gdSmallFont, $margins->{left}, $margins->{top} + $fonth, $studyName, $black);
    }

    my $graphBounds = $self->drawGraphAxes($tcData, $xparams, $yparams, $margins, $xAxisLabel, $drawHorizLines);
    my $plotnum = 1;

    foreach my $tcd (@$tcData) {
	$self->drawGraphPlot($graphBounds, $xparams, $yparams, $tcd, $plotnum++, $drawAreaUnderCurve, 3);
    }
}

# Draw the graph on the internal GD $image, based on expression data
# retrieved from the database.  This call is usually followed by a call 
# to getPng or getJpeg to retrieve the actual image data.
#
# $type       
#   type of data to display: 
#    'raw' - red channel raw values only
#    'relraw' - ratio of red channel raw value to time point #1
#    'rawboth' - red AND green channel raw values
#    'relrawboth' - ratio of red and green channel raw values to time point #1
#    'normr' - normalized ratios
#    'relnormr' - relative normalized ratios (i.e. ratio to time point #1)
# $studyName
#    the name of a time course study in rad3.Study
# $id
#     type type of id described by $idType
# $idType
#     either 'source_id' (i.e. a dots.GeneFeature source_id) or 'element_id" 
#     (from rad3.ElementImp)
# $projectId
#     optional; if specified will be used to join GeneFeature with ProjectLink
# $drawAreaUnderCurve
#     whether to shade the area(s) under the expression curve(s)
# $drawHorizLines
#     whether to draw horizontal guide lines in the background
#
sub drawGraphFromDb {
    my($self, $type, $studyName, $id, $idType, $projectId, $xAxisLabel, $drawAreaUnderCurve, $drawHorizLines, $extraData) = @_;

    my $shiftAmount = $self->{shiftAmount};
    my $normalizedData = ($type =~ /norm/i);
    my $relativeValues = ($type =~ /^rel/i);
    my $bothChannels = ($type =~/both$/i);

    # Get some colors and fonts
    # 
    my $image = $self->{image};

    my $black = $image->colorExact(0,0,0);

    my $blue = $image->colorExact(0,0,255);
    my $dblue = $image->colorExact(51,51,255);
    my $mblue = $image->colorExact(102,102,255);

    my $red = $image->colorExact(255,51,51);
    my $mred = $image->colorExact(255,102,102);

    my $purple = $image->colorExact(255,102,255);

    my $green = $image->colorExact(51,255,51);
    my $mgreen = $image->colorExact(102,255,102);
    my $dgreen = $image->colorExact(51,204,51);

    my $grey = $image->colorExact(204,204,204);
    my $mgrey = $image->colorExact(153,153,153);
    my $dgrey = $image->colorExact(102,102,102);

    # Time course data: an array of graphs to plot
    #
    my $tcData = [];
    my $geneDescr;

    # Data directly from the input
    #
    if ($extraData) {
	foreach my $ed (@$extraData) {
	    my $descr = $ed->{descr};
	    my $times = $ed->{times};
	    my $values =$ed->{values};
	    my $inputData = $self->getDataFromInput($descr, $times, $values);
	    push(@$tcData, @$inputData);
	}
    }

    # Two channels to plot - assume that time points, etc., are the same 
    #
    if ($bothChannels) {
	# Cy3
	my $ed1 = $self->getExpressionData($studyName, $id, $idType, $projectId, $normalizedData, 'Cy3');
	my $data1 = $ed1->{data};
	$geneDescr = $ed1->{geneDescr};
	my $compElts1 = $ed1->{compElts};
	my $ppData1 = $self->processTimeCourseData($data1);
	$ppData1 = $self->postProcessTimeCourseData($ppData1, $relativeValues, 0);
	$ppData1 = $self->shiftTimeCourseData($ppData1, $shiftAmount) if (defined($shiftAmount));
	my $descr1 = $self->getGraphDescription($normalizedData, $relativeValues, 'Cy3', $compElts1);
	push(@$tcData, { descr => "green plot: " . $descr1, 
			 htmlDescr => "<B>green plot</B>: " . $descr1, 
			 color => $mgreen, 
			 data => $ppData1 });

	# Cy5
	my $ed2 = $self->getExpressionData($studyName, $id, $idType, $projectId, $normalizedData, 'Cy5');
	my $data2 = $ed2->{data};
	my $compElts2 = $ed2->{compElts};
	my $ppData2 = $self->processTimeCourseData($data2);
	$ppData2 = $self->postProcessTimeCourseData($ppData2, $relativeValues, 0);
	$ppData2 = $self->shiftTimeCourseData($ppData2, $shiftAmount) if (defined($shiftAmount));
	my $descr2 = $self->getGraphDescription($normalizedData, $relativeValues, 'Cy5', $compElts2);
	push(@$tcData, { descr => "red plot: " . $descr2, 
			 htmlDescr => "<B>red plot</B>: " . $descr2, 
			 color => $mred, 
			 data => $ppData2 });
    } 

    # Only one channel (or ratios) to plot
    #
    else {
	my $ed = $self->getExpressionData($studyName, $id, $idType, $projectId, $normalizedData, 'Cy5');
	my $data = $ed->{data};
	my $compElts = $ed->{compElts};
	$geneDescr = $ed->{geneDescr};
	my $ppData = $self->processTimeCourseData($data);
	$ppData = $self->postProcessTimeCourseData($ppData, $relativeValues, $normalizedData);
	$ppData = $self->shiftTimeCourseData($ppData, $shiftAmount) if (defined($shiftAmount));
	my $descr = $self->getGraphDescription($normalizedData, $relativeValues, 'Cy5', $compElts);
	my $color = undef;
	my $colorName = undef;

	if ($normalizedData) {
	    # Use grey if we're overlaying the smoothed plot
	    if ($self->{showSmoothedPlot}) {
		$color = $grey;
		$colorName = 'grey';
	    } else {
		$color = $dblue;
		$colorName = 'blue';
	    }
	} else {
	    $color = $mred;
	    $colorName = 'red';
	}

	push(@$tcData, { descr => $colorName . " plot: " . $descr, 
			 htmlDescr => "<B>" . $colorName . " plot</B>: " . $descr, 
			 color => $color, 
			 lineColor => $color,
			 data => $ppData });

	# Add smoothed plot if requested
	#
	if ($self->{showSmoothedPlot}) {
	    $colorName = 'blue';
	    $color = $dblue;
	    my $smoothedData = &smoothTimeCourseData($ppData);
	    push(@$tcData, { descr => $colorName . " plot: Smoothed $descr ", 
			     htmlDescr => "<B>" . $colorName . " plot</B>: Smoothed $descr",
			     color => $color, 
			     data => $smoothedData });
	}
	
    }

    my $numPlots = scalar(@$tcData);
    my $xparams = $self->getXAxisParams($tcData);
    my $yparams = $self->getYAxisParams($tcData, $normalizedData, $relativeValues, 1);
    my $margins = $DEFAULT_MARGINS;

    # Display caption at the top describing the gene / study
    my $fonth = gdSmallFont->height;
    $image->string(gdSmallFont, $margins->{left}, $margins->{top}, $geneDescr, $black);
    $image->string(gdSmallFont, $margins->{left}, $margins->{top} + $fonth, $studyName, $black);

    my $graphBounds = $self->drawGraphAxes($tcData, $xparams, $yparams, $margins, $xAxisLabel, $drawHorizLines);

    my $plotnum = 1;
    foreach my $tcd (@$tcData) {
#	print STDERR "ExpressionGraph.pm: plotting $tcd->{descr} \n";
	$self->drawGraphPlot($graphBounds, $xparams, $yparams, $tcd, $plotnum++, $drawAreaUnderCurve, 3);
    }

    return $tcData;
}

# Draw the graph axes and captions
#
sub drawGraphAxes {
    my($self, $plots, $Xparams, $Yparams, $margins, $xAxisLabel, $drawHorizLines, $drawAxisLabels) = @_;
    my $numPlots = scalar(@$plots);

    my $minX = $Xparams->{min};
    my $maxX = $Xparams->{max};
    my $xTickInterval = $Xparams->{tickInterval};
    my $xTickLabelInterval = $Xparams->{tickLabelInterval};

    my $minY = $Yparams->{min};
    my $maxY = $Yparams->{max};
    my $yTickInterval = $Yparams->{tickInterval};
    my $yTickLabelInterval = $Yparams->{tickLabelInterval};

    my $image = $self->{image};
    my $width = $self->{width};
    my $height = $self->{height};
    my $font = gdSmallFont;

    my $noCaption = $self->{noCaption};
    my $noYAxisLabel = $self->{noYAxisLabel};

    my $fontqh = $font->height * 0.25;
    my $fonthh = $font->height * 0.50;
    my $fontqw = $font->width * 0.25;

    my $l1 = length($minY); my $l2 = length($maxY);

    # HACK - hard-code this so that the graphs will line up with one another
#    my $yAxisChars = ($l1 < $l2) ? $l2 : $l1;
    my $yAxisChars = 6;

    # Colors
    #
    my $black = $image->colorExact(0,0,0);
    my $grey = $image->colorExact(204,204,204);
    my $dgrey = $image->colorExact(102,102,102);

    # Bounds of the graph drawing area

    # enough space for largest y-axis label
    my $graphXMin = $margins->{left} + ($font->width * ($yAxisChars + 1)) + $fontqw;  
    # nothing on right side of graph
    my $graphXMax = $width - ($margins->{right});
    # 1 line of text - caption describing study
    my $graphYMin = $margins->{top} + ($font->height * ($noCaption ? 0 : 3));
    # 1 + N lines of text, where N = number of plots
    # TO DO - using 2 instead of $numPlots
    my $mf = $drawAxisLabels ? 4.25 : 1.25;
    my $graphYMax = $height - ($margins->{bottom} + ($noYAxisLabel ? 0 : (($font->height * $mf) + $fonthh))); 
    
    # y-axis
    #
#    my $yaxis = new GD::Polygon;
#    $yaxis->addPt($graphXMin,$graphYMin);
#    $yaxis->addPt($graphXMin+1,$graphYMin);
#    $yaxis->addPt($graphXMin+1,$graphYMax);
#    $yaxis->addPt($graphXMin,$graphYMax);
#    $image->filledPolygon($yaxis, $black);
    $image->line($graphXMin-1, $graphYMax, $graphXMin-1, $graphYMin, $black);

    # x-axis
    #
#    my $xaxis = new GD::Polygon;
#    $xaxis->addPt($graphXMin,$graphYMax);
#    $xaxis->addPt($graphXMax,$graphYMax);
#    $xaxis->addPt($graphXMax,$graphYMax+1);
#    $xaxis->addPt($graphXMin,$graphYMax+1);
#    $image->filledPolygon($xaxis, $black);
    $image->line($graphXMin, $graphYMax+1, $graphXMax, $graphYMax+1, $black);

    my $fontqh = $font->height * 0.25;
    my $fonthh = $font->height * 0.50;

    # label x-axis with time points
    #
    if (!$noYAxisLabel) {
	my $npoints = $maxX - $minX + 1;

	my $xaxis_len = $graphXMax - $graphXMin;
	my $xspace = $xaxis_len / $npoints;
	
	for (my $i = $minX;$i <= $maxX;$i += $xTickInterval) {
	    my $xpos = $graphXMin + ($xspace * $i);
	    my $tick = new GD::Polygon;
	    $tick->addPt($xpos,$graphYMax+1);
	    $tick->addPt($xpos+1,$graphYMax+1);
	    $tick->addPt($xpos+1,$graphYMax+4);
	    $tick->addPt($xpos,$graphYMax+4);
	    $image->filledPolygon($tick, $black);
	    
	    # Label even-numbered hours
	    #
	    if ($i % $xTickLabelInterval == 0) {
		my $halfw = $font->width * length($i) * 0.5;
		$image->string($font, $xpos - $halfw + 1, $graphYMax + 5, $i, $black);
	    }
	}

	# Display captions at the bottom describing the y-axis and data plots
	#
	if ($drawAxisLabels) {
	    $image->string($font, $graphXMin, $graphYMax + $font->height + $fonthh, "x-axis: " . $xAxisLabel, $black);
	    
	    my $ i = 1;
	    foreach my $plot (@$plots) {
		my $descr = $plot->{descr};
		$image->string($font, $graphXMin, $graphYMax + ($font->height * (1 + $i++) + $fonthh), $descr, $black);
	    }
	}
    }

    my $fonthh = $font->height * 0.5;
    my $yspace = $graphYMax - $graphYMin;

    # label y-axis
    #
    for (my $i = $minY;$i <= $maxY;$i += $yTickInterval) {
	my $frac = ($i - $minY) / ($maxY - $minY);
	my $ypos = $graphYMax - ($frac * $yspace);

#	print STDERR "ExpressionGraph: labelling y-axis point $i at $graphXMin, $ypos yspace = $yspace maxY=$maxY\n";

	my $tick = new GD::Polygon;
	$tick->addPt($graphXMin-1, $ypos+1);
	$tick->addPt($graphXMin-4, $ypos+1);
	$tick->addPt($graphXMin-4, $ypos);
	$tick->addPt($graphXMin-1, $ypos);
	$image->filledPolygon($tick,$black);

	# TO DO - don't draw lines so frequently?

	if (($drawHorizLines) && ($ypos < $graphYMax)) {
	    $image->line($graphXMin + 1, $ypos, $graphXMax, $ypos, ($i == 0) ? $dgrey: $grey);
	}

	if ($i % $yTickLabelInterval == 0) {
	    my $xpos = $graphXMin - $font->width * length($i) - 6;
	    $image->string($font, $xpos, $ypos - $fonthh, $i, $black);
	}
    }

    return { x1 => $graphXMin, x2 => $graphXMax, y1 => $graphYMin, y2 => $graphYMax };
}

# Draw a single plot on the graph
#
# $maxConnectDist   Maximum distance between two successive points for them to be connected with
#                   a line.  If set to 0 then no connection is done, if set to 2 then one point
#                   can be skipped, etc.
#
sub drawGraphPlot {
    my($self, $bounds, $xparams, $yparams, $tcData, $plotnum, $drawAreaUnderCurve, $maxConnectDist) = @_;

    my $data = $tcData->{data};
    my $xspace = $bounds->{x2} - $bounds->{x1};
    my $yspace = $bounds->{y2} - $bounds->{y1};

    my $image = $self->{image};
    my $shadeColor = $image->colorExact(split(/,/, $self->{shadeColor}));
    my $lineColor = undef;

    if ($tcData->{lineColor}) { 
	$lineColor = $tcData->{lineColor};
    } else {
	$lineColor = $image->colorExact(split(/,/, $self->{lineColor}));
    }

    my $minY = $yparams->{min};
    my $maxY = $yparams->{max};

    my $minX = $xparams->{min};
    my $maxX = $xparams->{max};
    my $xgap = $xspace / ($maxX - $minX + 1);

    # Coordinates of the points to be plotted
    #
    my $coords = [];

    foreach my $point (@$data) {
	my $time = $point->{time};
	my $val = $point->{value};

#	print STDERR "ExpressionGraph.pm: time $time => $val\n";

        if ($val =~ /\S/) {
	    my $xpos = $bounds->{x1} + ($xgap * $time);
            my $frac = ($val - $minY) / ($maxY - $minY);
            my $ypos = $bounds->{y2} - ($frac * $yspace);

	    push(@$coords, [$time, $xpos, $ypos]);
	}
    }
    my $nc = scalar(@$coords);

    # 1. Shade area under the curve
    #
    if ($drawAreaUnderCurve) {
	my $area = new GD::Polygon;

	for (my $i = 0;$i < $nc;++$i) {
	    my($time, $xpos, $ypos) = @{$coords->[$i]};

	    if ($i == 0) { $area->addPt($xpos, $bounds->{y2}); }
	    $area->addPt($xpos, $ypos);
	    if ($i == ($nc - 1)) { $area->addPt($xpos, $bounds->{y2}); }
	}
	$image->filledPolygon($area, $shadeColor);
    }

    # 2. Draw the curve and the colored data points on the curve

    # draw the lines
    my $lastx = undef; my $lasty = undef; my $lastTime = undef;
    for (my $i = 0;$i < $nc;++$i) {
	my($time, $xpos, $ypos) = @{$coords->[$i]};

	if ($lastx) {
	    my $dist = $time - $lastTime;

	    # Only connect points closer than $maxConnectDist
	    #
	    if ($dist <= $maxConnectDist) {
		$image->line($lastx, $lasty, $xpos, $ypos, $lineColor);
	    }
	}
	$lastx = $xpos; $lasty = $ypos; $lastTime = $time;
    }

    # draw the data points (squares)
    #
    if (!$self->{noSquares}) {
	my $ss = $self->{square_size};

	for (my $i = 0;$i < $nc;++$i) {
	    my($time, $xpos, $ypos) = @{$coords->[$i]};
	    
	    my $square = new GD::Polygon;
	    
	    $square->addPt($xpos-$ss, $ypos-$ss);
	    $square->addPt($xpos+$ss, $ypos-$ss);
	    $square->addPt($xpos+$ss, $ypos+$ss);
	    $square->addPt($xpos-$ss, $ypos+$ss);
	    
#       $image->polygon($square, $ptColor);
	    $image->filledPolygon($square, $tcData->{color});
	}
    }
}

# x-axis displays the time points in the timecourse study
#
sub getXAxisParams {
    my($self, $tcData) = @_;
    my($min, $max);

    # Find min and max
    #
    foreach my $tcd (@$tcData) {
	my $data = $tcd->{data};
	my $np = scalar(@$data);
	my $firstTime = $data->[0]->{time};
	my $lastTime = $data->[$np - 1]->{time};
	$min = $firstTime if (!defined($min) || ($firstTime < $min));
	$max = $lastTime if (!defined($max) || ($lastTime > $max));
    }

    # Hack - assume that any time course that starts with time t = 1 is in fact
    # relative to some event at time t = 0 (where no measurement was made.)
    #
    $min = 0 if ($min == 1);
    return { min => $min, max => $max, tickInterval => 1, tickLabelInterval => 4 };
}

# y-axis displays the expression values (raw or normalized, single channel or ratios)
#
sub getYAxisParams {
    my($self, $tcData, $normalizedData, $relativeValues, $computeFromData, $minY, $maxY) = @_;
    my($min, $max, $tickInterval, $tickLabelInterval);

    # Compute y-axis parameters specific to this gene, based on the data
    #
    if ($computeFromData) {
	foreach my $tcd (@$tcData) {
	    my $data = $tcd->{data};
	    foreach my $pt (@$data) {
		my $val = $pt->{value};
		$min = $val if (!defined($min) || $val < $min);
		$max = $val if (!defined($max) || $val > $max);
	    }
	}
	my $dist = ($max - $min + 1);

	# HACK
	if ($dist < 20) {

#	    print STDERR "ExpressionGraph.pm: dist=$dist min=$min, max=$max\n";

	    my $absMax = abs($max);
	    my $absMin = abs($min);
	    my $largest = ($absMax > $absMin) ? $absMax : $absMin;
	    $largest = int($largest+1);

	    # Don't go smaller than 2 because we don't want to exagerate the noise too much.
	    #
	    $largest = 2 if ($largest < 2);

	    $min = -$largest;
	    $max = $largest;

	    $tickInterval = 1;
	    $tickLabelInterval = 1;
	} 
	else {
	    $min = 0 if ($min > 0);
	    my $mag = int((log($max) / log(10)) + 1);
	    my $pow = 10 ** ($mag - 1);
	    my $newmax = int($max / $pow + 1) * $pow;
	    
#	    print STDERR "ExpressionGraph.pm: min=$min, max=$max, mag=$mag, pow=$pow newmax=$newmax\n";
	    
	    $max = $newmax;
	    $tickInterval = $newmax / 10;
	    $tickLabelInterval = $newmax / 5;
	}
    }

    # Use specified minima and maxima
    #
    elsif ($minY =~ /\d/) {
	$min = $minY;
	$max = $maxY;

	if (($max - $min) < 15) {
	    $tickInterval = 1;
	    $tickLabelInterval = 1;
	} else {
	    $tickInterval = ($max - $min);
	    $tickLabelInterval = ($max - $min);
	}
    }

    # Use default y-axis parameters; useful when displaying a set of graphs side
    # by side.
    #
    else {
    
	# TO DO - run queries to get the correct values:
	# 
	if ($normalizedData) {
	    if ($relativeValues) {
		$min = -7;
		$max = 7;
		$tickInterval = 1;
		$tickLabelInterval = $max;
	    } 
	    else {
		$min = -7;
		$max = 7;
		$tickInterval = 1;
		$tickLabelInterval = $max;
	    }
	} 
	else {
	    
	    # TO DO - query for these
	    #
	    if ($relativeValues) {
		$min = 0;
		$max = 6;
		$tickInterval = 1;
		$tickLabelInterval = 1;
	    } 
	    
	    # Raw unnormalized values; these go as high as 45,000 according to the paper
	    #
	    else {
		$min = 0;
		$max = 20000;
		$tickInterval = $max / 10;
		$tickLabelInterval = $max / 5;
	    }
	}
    }
    return { min => $min, max => $max, tickInterval => $tickInterval, tickLabelInterval => $tickLabelInterval };
}

sub getPng {
    my($self) = @_;
    my $img = $self->{image};
    return $img->can('png') ? $img->png() : $img->gif();
}

sub getJpeg {
    my($self, $quality) = @_;
    my $img = $self->{image};
    if (!$img->can('jpeg')) { return $self->getPng(); }
    return $img->jpeg($quality) if (defined($quality));
    return $img->jpeg();
}

# Get normalized or unnormalized expression data for a single gene in a given
# rad3 Study.
#
# $idType
#     either 'source_id' (i.e. a dots.GeneFeature source_id) or 'element_id" 
#     (from rad3.ElementImp)

sub getExpressionData {
    my($self, $studyName, $id, $idType, $projectId, $normalizedData, $channel) = @_;
    my $data = [];

    my($studyId, $studyName) = $self->getStudy($studyName);

    # Get all assays in this study
    #
    my $assays = $self->getStudyAssays($studyId);

#    print STDERR "ExpressionGraph.pm: Found ", scalar(@$assays), " assay(s) for study '$studyName'\n";

    # Determine the subclass of CompositeElementImp used by each of the
    # assays (they should typically all be the same).
    #
    my $arrayIdHash = {};
    foreach my $assay (@$assays) {
	$arrayIdHash->{$assay->{array_id}} = 1;
    }
    my @arrayIds = keys %$arrayIdHash;

#    my $compEltViews = $self->getCompositeElementViews(\@arrayIds);
#    print STDERR "ExpressionGraph.pm: Found ", scalar(@arrayIds), " distinct array(s) for study '$studyName'\n";

    # Get composite_element_ids for this gene
    #
    my $compElts;
    my $geneString;

    if ($idType =~ /source_id/i) {
	$compElts = $self->getGeneCompositeElements($id, $projectId, \@arrayIds);
	$geneString = "PlasmoDB gene $id / Array element(s): " . join(',', map {$_->{source_id}} @$compElts);
    } else {
	$compElts = $self->getElementCompositeElements($id, $projectId, \@arrayIds);
	$geneString = "Array element(s): " . join(',', map {$_->{source_id}} @$compElts);
    }

#    print STDERR "ExpressionGraph.pm: 1. $geneString\n";
#    print STDERR "ExpressionGraph.pm: 2. Mapped $id -> ", join(',', map {$_->{source_id}} @$compElts), "\n";

    # Get all normalized data, indexed by assay id
    #
    my @assayIds = map { $_->{assay_id} } @$assays;
    my @compEltIds = map { $_->{composite_element_id} } @$compElts;
    my $expData;

#    print STDERR "ExpressionGraph.pm: 3. Mapped $id -> ", join(',', @compEltIds), "\n";

    if ($normalizedData) {
	$expData = $self->getNormalizedData(\@assayIds, \@compEltIds);
    } else {
	$expData = $self->getRawData(\@assayIds, \@compEltIds, $channel);
    }

    foreach my $assay (@$assays) {
	my $arrayId = $assay->{array_id};
	my $assayId = $assay->{assay_id};
	my $assayName = $assay->{name};

	# All data values for this assay (even if there are multiple composite elements)
	#
	my $allData = [];

	# Get expression value(s) for each composite element
	#
	foreach my $compElt (@$compElts) {
	    my $compEltId = $compElt->{composite_element_id};
	    my $nd = $expData->{$assayId}->{$compEltId};
	    if (defined($nd)) {
		push(@$allData, @$nd);
	    }
	}
	push(@$data, { assay_name => $assayName, data => $allData });
    }
    return { data => $data, geneDescr => $geneString, compElts => $compElts };
}

# Parse the assay descriptions returned by getExpressionData to associate expression
# values with time points.  Averages data from the same time point.
#
# TO DO - make the regular expression a parameter
#
sub processTimeCourseData {
    my($self, $data) = @_;
    my $tpHash = {};
   
    foreach my $datum (@$data) {
	my $assay = $datum->{assay_name};
	my $expData = $datum->{data};
	my($time) = ($assay =~ /time point (\d+)\S*/);

#	print STDERR "ExpressionGraph.pm: time=$time data=", join(',', @$expData), "\n";

	if (!($time =~ /\d/)) {
	    print STDERR "ExpressionGraph.pm: ERROR - unable to parse time from '$assay'\n";
	} else {
	    my $list = $tpHash->{$time};
	    if (defined($list)) {
		push(@$list, @$expData);
	    } else {
		$tpHash->{$time} = $expData;
	    }
	}
    }

    my $result = [];

    foreach my $k (sort {$a <=> $b} keys %$tpHash) {
	my $data = $tpHash->{$k};
	my $avg = $self->avg($data);
	push(@$result, { time => $k, value => $avg });

#	print STDERR "ExpressionGraph.pm: time=$k, data=", join(',', @$data), " avg=$avg\n";
    }

    return $result;
}

# Post-process timecourse data; normalize to time point 1, if requested.
#
sub postProcessTimeCourseData {
    my($self, $tcData, $relativeValues, $logScaleData) = @_;

    if ($relativeValues) {
	my $numTimes = scalar(@$tcData);
	my $firstTime = $tcData->[0]->{time};
	my $lastTime = $tcData->[$numTimes-1]->{time};

	# Post process time course data to display ratios relative to time point 1,
	# if $type =~ /^relative/
	#
	if ($logScaleData) {
	    my $tp1 = $tcData->[0]->{value};
	    foreach my $tc (@$tcData) {
		$tc->{value} = $tc->{value} - $tp1;
	    }
	} else {
	    my $tp1 = $tcData->[0]->{value};
	    foreach my $tc (@$tcData) {
		$tc->{value} = $tc->{value} / $tp1;
	    }
	}
    }

    return $tcData;
}

sub shiftTimeCourseData {
    my($self, $tcData, $shiftAmount) = @_;

    my $minTime = $self->{minTime};
    my $maxTime = $self->{maxTime};

    my $numTimes = scalar(@$tcData);
    my @shiftedData;

    foreach my $tc (@$tcData) {
	my $newTime = (($tc->{time} - $minTime + $shiftAmount) % $maxTime) + $minTime;
	my $newVal = { 
	    time => $newTime,
	    value => $tc->{value}
	};
	push(@shiftedData, $newVal);
    }
    
    my @sorted = sort { $a->{time} <=> $b->{time} } @shiftedData;
    return \@sorted;
}

sub avg {
    my($self, $data) = @_;
    my $nd = scalar(@$data);
    my $sum = 0;
    foreach my $d (@$data) { $sum += $d; }
    return ($nd == 0) ? undef : ($sum / $nd);
}

# Return all composite elements associated with a gene
#
sub getGeneCompositeElements {
    my($self, $geneFeatSrcId, $projectId, $arrayIds) = @_;
    my $dbh = $self->{dbh};
    my $coreDb = $self->{coreDb};
    my $dotsDb = $self->{dotsDb};
    my $radDb = $self->{radDb};
    my $compElts = [];
    my $haveProj = ($projectId =~ /\d/);

    my $sql = ("select distinct ce.composite_element_id, ce.source_id " .
	       "from ${dotsDb}.GeneFeature gf, ${radDb}.CompositeElementGUS ceg, " .
	       "${coreDb}.TableInfo ti, ${radDb}.CompositeElementImp ce " .
	       ($haveProj ? ", ${dotsDb}.ProjectLink pl " : "") .
	       "where gf.source_id = '$geneFeatSrcId' " .
	       ($haveProj ? "and pl.id = gf.na_feature_id and pl.table_id = ti.table_id and pl.project_id = $projectId " : "") .
	       "and gf.na_feature_id = ceg.row_id " .
	       "and ceg.table_id = ti.table_id " .
	       "and ti.name = 'GeneFeature' " .
	       "and ceg.composite_element_id = ce.composite_element_id " .
	       "and ce.array_id in (" . join(',', @$arrayIds) . ")");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while(my $h = $sth->fetchrow_hashref('NAME_lc')) {
	my %copy = %$h;
	push(@$compElts, \%copy);
    }

    $sth->finish();
    return $compElts;
}

# Return all RAD3 Spots associated with a gene
#
sub getGeneSpots {
    my($self, $geneFeatSrcId, $projectId, $arrayIds) = @_;
    my $dbh = $self->{dbh};
    my $coreDb = $self->{coreDb};
    my $dotsDb = $self->{dotsDb};
    my $radDb = $self->{radDb};
    my $spots = [];
    my $haveProj = ($projectId =~ /\d/);

    my $sql = ("select distinct ce.source_id, s.element_id, s.name, " .
	       " s.array_row, s.array_column, s.grid_row, s.grid_column, s.sub_row, s.sub_column " .
	       "from ${dotsDb}.GeneFeature gf, ${radDb}.CompositeElementGUS ceg, " .
	       "${coreDb}.TableInfo ti, ${radDb}.CompositeElementImp ce, ${radDb}.Spot s " .
	       ($haveProj ? ", ${dotsDb}.ProjectLink pl " : "") .
	       "where gf.source_id = '$geneFeatSrcId' " .
	       ($haveProj ? "and pl.id = gf.na_feature_id and pl.table_id = ti.table_id and pl.project_id = $projectId " : "") .
	       "and gf.na_feature_id = ceg.row_id " .
	       "and ceg.table_id = ti.table_id " .
	       "and ti.name = 'GeneFeature' " .
	       "and ceg.composite_element_id = ce.composite_element_id " .
	       "and ce.array_id in (" . join(',', @$arrayIds) . ")" .
	       "and ce.composite_element_id = s.composite_element_id ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while(my $h = $sth->fetchrow_hashref('NAME_lc')) {
	my %copy = %$h;
	push(@$spots, \%copy);
    }

    $sth->finish();
    return $spots;
}

# Return all composite elements associated with an element
#
sub getElementCompositeElements {
    my($self, $elementId, $projectId, $arrayIds) = @_;
    my $dbh = $self->{dbh};
    my $radDb = $self->{radDb};
    my $compElts = [];
    my $haveProj = ($projectId =~ /\d/);

    my $sql = ("select distinct ce.composite_element_id, ce.source_id " .
	       "from ${radDb}.ElementImp ei, ${radDb}.CompositeElementImp ce " .
	       "where ei.element_id = $elementId " .
	       "and ei.composite_element_id = ce.composite_element_id " .
	       "and ei.array_id in (" . join(',', @$arrayIds) . ")");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while(my $h = $sth->fetchrow_hashref('NAME_lc')) {
	my %copy = %$h;
	push(@$compElts, \%copy);
    }
    $sth->finish();

    # DEBUG
    my $ne = scalar(@$compElts);

    # look for empty source_ids
    foreach my $ce (@$compElts) {
	if ($ce->{source_id} =~ /^\s*$/) {
	    print STDERR "ExpressionGraph.pm: WARNING - element $elementId has CompositeElement with empty source_id\n";
	    $ce->{source_id} = '?';
	}
    }

    return $compElts;
}

# Return all of the assays that belong to a given study.
#
sub getStudy {
    my($self, $studyName) = @_;
    my $dbh = $self->{dbh};
    my $radDb = $self->{radDb};
    my @result;

    my $sql = ("select s.study_id, s.name " .
	       "from ${radDb}.Study s " .
	       "where upper(s.name) = upper('$studyName') ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    if(my @a = $sth->fetchrow_array()) {
	@result = @a;
    }

    $sth->finish();
    return @result;
}

# Return all of the assays that belong to a given study.
#
sub getStudyAssays {
    my($self, $studyId) = @_;
    my $dbh = $self->{dbh};
    my $radDb = $self->{radDb};
    my $assays = [];

    my $sql = ("select a.source_id, a.name, a.assay_id, a.array_id "  .
	       "from ${radDb}.StudyAssay sa, ${radDb}.Assay a " .
	       "where sa.study_id = $studyId " .
	       "and sa.assay_id = a.assay_id ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while(my $h = $sth->fetchrow_hashref('NAME_lc')) {
	my %copy = %$h;
	push(@$assays, \%copy);
    }

    $sth->finish();
    return $assays;
}

# Return the CompositeElement view names (i.e. subclass_view) for 
# a set of arrays.
#
sub getCompositeElementViews {
    my($self, $arrayIds) = @_;
    my $dbh = $self->{dbh};
    my $radDb = $self->{radDb};
    my $views = {};

    my $sql = ("select distinct array_id, subclass_view " .
	       "from ${radDb}.CompositeElementImp " .
	       "where array_id in ( " . join(',', @$arrayIds) . ")");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while (my($array_id, $sv) = $sth->fetchrow_array()) {
	if ($views->{$array_id}) {
	    print STDERR "ExpressionGraph.pm: WARNING - array_id $array_id maps to multiple views of CompositeElementImp\n";
	}

	$views->{$array_id} = $sv;
    }

    $sth->finish();   
    return $views;
}

# Get normalized data for a CompositeElement in a particular Assay.
#
# IMPORTANT: Assumes only one Acquisition and one Quantification 
# are in the database for each assay (or two for 2-channel data).
#
# TO DO: make the pi.description 'LIKE' clause a parameter
#
sub getNormalizedData {
    my($self, $assayIds, $compEltIds) = @_;
    my $dbh = $self->{dbh};
    my $radDb = $self->{radDb};
    my $normData = {};
    my $norm = $self->{normalization};

    my $sql = ("select distinct ac.assay_id, s.composite_element_id, pi.process_invocation_id, pr.value " .
	       "from ${radDb}.spot s, ${radDb}.acquisition ac, ${radDb}.quantification q,  " .
	       "     ${radDb}.elementresultimp eri, " .
	       "     ${radDb}.processinvocation pi, ${radDb}.processio pio, ${radDb}.processresult pr " .
	       "where ac.assay_id in (" . join(',', @$assayIds) . ") " .
	       "and ac.acquisition_id = q.acquisition_id " .
	       "and s.composite_element_id in (" . join(',', @$compEltIds) . ") " .
	       "and s.element_id = eri.element_id " .
	       "and eri.element_result_id = pio.input_result_id " .
	       "and eri.quantification_id = q.quantification_id " .
	       "and pio.process_invocation_id = pi.process_invocation_id " .

	       # HACK - omit normalizations that we think are bogus.  Need way to 
	       # group normalization runs in RAD.
	       "and pi.process_invocation_id " . (($norm =~ /new/i) ? " >= 709 " : " < 528 ") .
	       "and pi.process_invocation_id not in (375,429) " .
	       "and (pi.description like 'Print Tip Lowess normalization of%' " .
	       "     or pi.description like 'Print Tip Loess normalization of%') " .
	       "and pio.output_result_id = pr.process_result_id " . 
	       "order by ac.assay_id ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while(my($assayId, $compEltId, $pid, $value) = $sth->fetchrow_array()) {
	my $al = $normData->{$assayId};
	if (!defined($al)) {
	    $al = {};
	    $normData->{$assayId} = $al;
	}

	my $cl = $al->{$compEltId};
	if (!defined($cl)) {
	    $cl = [ $value ];
	    $al->{$compEltId} = $cl;
	} else {
	    push(@$cl, $value);
	}
    }

    $sth->finish();
    return $normData;
}

# Get normalized data for a CompositeElement in a particular Assay.
#
# IMPORTANT: Assumes only one Acquisition and one Quantification 
# are in the database for each assay (or two for 2-channel data).
#
# TO DO - Generalize this to work with views other than GenePixElementResult,
#         or fix the database so that ElementResultImp can be used.  Problems
#         with the database include:
#            rad3.quantification.result_table_id can be (and is) null
#            rad3.elementresultimp.foreground is null for (at least some of)
#             the GenePixElementResult data
#
sub getRawData {
    my($self, $assayIds, $compEltIds, $channel) = @_;
    my $dbh = $self->{dbh};
    my $radDb = $self->{radDb};
    my $rawData = {};

    my $sql = ("select distinct ac.assay_id, s.composite_element_id, eri.foreground_median " .
	       "from ${radDb}.spot s, ${radDb}.acquisition ac, ${radDb}.channel c, ${radDb}.quantification q,  " .
	       "     ${radDb}.GenePixElementResult eri " .
	       "where ac.assay_id in (" . join(',', @$assayIds) . ") " .
	       "and ac.channel_id = c.channel_id " .
	       "and c.name = '$channel' " .
	       "and ac.acquisition_id = q.acquisition_id " .
	       "and s.composite_element_id in (" . join(',', @$compEltIds) . ") " .
	       "and s.element_id = eri.element_id " .
	       "and eri.quantification_id = q.quantification_id " .
	       "order by ac.assay_id ");

    my $sth = $dbh->prepare($sql);
    $sth->execute();

    while(my($assayId, $compEltId, $value) = $sth->fetchrow_array()) {
	my $al = $rawData->{$assayId};
	if (!defined($al)) {
	    $al = {};
	    $rawData->{$assayId} = $al;
	}

	my $cl = $al->{$compEltId};
	if (!defined($cl)) {
	    $cl = [ $value ];
	    $al->{$compEltId} = $cl;
	} else {
	    push(@$cl, $value);
	}
    }

    $sth->finish();
    return $rawData;
}

sub getGraphDescription {
    my($self, $normalized, $relative, $channel, $elements) = @_;
    my $descr = $normalized ? "Normalized log (base 2) ratio of Cy5/Cy3" : "Raw ${channel}-channel intensity (foreground median)";
    $descr .= ", relative to time t=1" if ($relative);
    my $elts = join(',', map { $_->{'source_id'} } @$elements);
    $descr .= " for $elts";
    return $descr;
}

sub smoothTimeCourseData {
    my($tcd) = @_;
    my $numTimes = scalar(@$tcd);
    my @values;

    foreach my $tc (@$tcd) {
	push(@values, $tc->{value});
    }

    my $newValues = &smoothData(\@values);
    my $result = [];
    my $ind = 0;
    foreach my $tc (@$tcd) {
	push(@$result, { time => $tc->{time}, value => $newValues->[$ind++] });
    }

    return $result;
}

# Data smoothing subroutine provided by Greg Grant
#
sub smoothData {
   my ($vector) = @_;
   my $veclen = @$vector;

    if ($veclen < 6) {
	die "vector is too short to smooth, must be at least length six\n";
    }

    my @temp1;
    my @temp2;
    my $a;
    my $b;
    my @smoothed_vector;

   #  the case 0

    $temp1[0]=$vector->[0];
    $temp1[1]=$vector->[1];
    $temp1[2]=$vector->[2];
    $temp2[0]=1;
    $temp2[1]=2;
    $temp2[2]=3;

    ($a, $b) = LS(\@temp2, \@temp1);
    $smoothed_vector[0]=$a+$b;
    
   #  the case n (n=vector length)

    $temp1[0]=$vector->[$veclen-3];
    $temp1[1]=$vector->[$veclen-2];
    $temp1[2]=$vector->[$veclen-1];
    ($a, $b) = LS(\@temp2, \@temp1);
    $smoothed_vector[$veclen-1]=$a*3+$b;

   #  the case 1

    $temp1[0]=$vector->[0];
    $temp1[1]=$vector->[1];
    $temp1[2]=$vector->[2];
    $temp1[3]=$vector->[3];
    $temp2[0]=1;
    $temp2[1]=2;
    $temp2[2]=3;
    $temp2[3]=4;

    ($a, $b) = LS(\@temp2, \@temp1);

    $smoothed_vector[1]=$a*2+$b;

   #  the case n-1 (n=vector length)

    $temp1[0]=$vector->[$veclen-4];
    $temp1[1]=$vector->[$veclen-3];
    $temp1[2]=$vector->[$veclen-2];
    $temp1[3]=$vector->[$veclen-1];
    ($a, $b) = LS(\@temp2, \@temp1);
    $smoothed_vector[$veclen-2]=$a*3+$b;

   # the rest of the cases

    for(my $i=2; $i<$veclen-2; $i++) {

	$temp1[0]=$vector->[$i-2];
	$temp1[1]=$vector->[$i-1];
	$temp1[2]=$vector->[$i];
	$temp1[3]=$vector->[$i+1];
	$temp1[4]=$vector->[$i+2];
	$temp2[4]=5;

	($a, $b) = LS(\@temp2, \@temp1);
	$smoothed_vector[$i]=$a*3+$b;
    }

    return \@smoothed_vector;
}

# Least Squares routine provided by Greg Grant
#
# $Xref - reference to first vector
# $Yref - reference to second vector; must be same length as $Xref
#
sub LS {
    my ($Xref, $Yref)=@_;
    
    my $X_vect_length=@$Xref;
    my $Y_vect_length=@$Yref;

    if ($X_vect_length != $Y_vect_length) {
	print STDERR "ERROR: tried to run the LS subroutine with vectors of differing lengths\n";
	return undef;
    }

    my $y=0;
    my $x=0;
    my $yy=0;
    my $xx=0;
    my $xy=0;

    for(my $i=0; $i<$X_vect_length;$i++) {
	$x += $Xref->[$i];
	$y += $Yref->[$i];
	$xx += ($Xref->[$i])*($Xref->[$i]);
	$yy += ($Yref->[$i])*($Yref->[$i]);
	$xy += ($Xref->[$i]*$Yref->[$i]);
    }    

    my $a = ($y*$xx-$x*$xy)/($X_vect_length*$xx-$x*$x);
    my $b = ($X_vect_length*$xy-$x*$y)/($X_vect_length*$xx-$x*$x);

    return($b,$a);
}


1;

