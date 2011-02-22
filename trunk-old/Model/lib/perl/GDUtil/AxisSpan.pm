#!/usr/bin/perl

#------------------------------------------------------------------------
# AxisSpan.pm
#
# A subclass of StripeSpan that represents the coordinate system in a 
# GDUtil-based sequence display.  The coordinate system is typically 
# either a protein or DNA sequence, and all the annotation objects are
# stored as children of the AxisSpan.
#
# Jonathan Crabtree
# 
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::AxisSpan;

use strict;

use GD;
use WDK::Model::GDUtil::Span;
use WDK::Model::GDUtil::PrecomputedTickParams;

@WDK::Model::GDUtil::AxisSpan::ISA = ('WDK::Model::GDUtil::StripeSpan');

#-------------------------------------------------
# Configuration
#-------------------------------------------------

# HACK - turn on debugging to STDERR
my $DEBUG = 0;

# The ticks are marks on the axis used to show the scale (e.g., 5kb, 10kb, 15kb ,etc.)
#
my $DFLT_TICK_HEIGHT = 5;
my $DFLT_TICK_WIDTH = 2;
my $DFLT_TICK_INTERVAL = 'ends';
my $DFLT_TICK_LABEL = 'bp';

#-------------------------------------------------
# AxisSpan
#-------------------------------------------------

sub new {
    my($class, $span) = @_;
    my $self = $class->SUPER::new($span);

    my $th = $span->{tickHeight};
    my $tw = $span->{tickWidth};
    my $ti = $span->{tickInterval};
    my $tl = $span->{tickLabel};
    my $ticks = $span->{ticks};

    $self->{tickHeight} = defined($th) ? $th : $DFLT_TICK_HEIGHT;
    $self->{tickWidth} = defined($tw) ? $tw : $DFLT_TICK_WIDTH;
    $self->{tickLabel} = defined($tl) ? $tl : $DFLT_TICK_LABEL;
    $self->{tickInterval} = defined($ti) ? $ti : $DFLT_TICK_INTERVAL;
    $self->{ticks} = $ticks if defined($ticks);
    $self->{font} = gdSmallFont;
    $self->{labelVAlign} = 'bottom';

    bless($self, $class);
    return $self;
}

#-------------------------------------------------
# Span
#-------------------------------------------------

sub getSelfHeight {
    my($self) = @_;
    return $self->{height} + $self->{tickHeight} + $self->{font}->height;
}

sub drawSelf {
    my($self, $gdCanvas) = @_;
    my $wcs = [$self->{x1}, $self->{x2}];

    my $scs = $gdCanvas->worldToScreenArray($wcs);
    my $image = $gdCanvas->getImage();
    my $color = $gdCanvas->getDefaultFgColor();
    my $y1 = $self->{y1} - $self->getSelfHeight();

    # A narrow filled rectangle for the axis
    #
    my $rect = new GD::Polygon;
    $rect->addPt($scs->[0], $y1);
    $rect->addPt($scs->[1], $y1);
    $rect->addPt($scs->[1], $y1 + $self->{height});
    $rect->addPt($scs->[0], $y1 + $self->{height});
    $image->filledPolygon($rect, $color);

    # Labeled tick marks
    #
    my $tw = $self->{tickWidth};
    my $th = $self->{tickHeight};
    my $ti = $self->{tickInterval};
    my $ht = $self->{height};

    my $tickW = [];
    my $tickLabels = [];

    # an array of tick locations has been supplied
    if (defined($self->{ticks})) {
	foreach my $t (@{$self->{ticks}}) {
	    push(@$tickW, $t);
	}
    } 
    # display tick marks only at the ends of the axis (but make the first one at 1, not 0)
    elsif ($ti =~ /endsplusone/i) {
	push(@$tickW, $wcs->[0] + 1);
	push(@$tickW, $wcs->[1]);
    } 
    # display tick marks only at the ends of the axis
    elsif ($ti =~ /ends/i) {
	push(@$tickW, $wcs->[0]);
	push(@$tickW, $wcs->[1]);
    } 
    # 'auto' mode for tick marks; based on old bioWidget code
    elsif ($ti =~ /auto/i) {               
	($tickW, $tickLabels) = $self->autocomputeTicks($wcs, $scs, 5);
    } 
    # default is to place a tick every $ti units
    else {
	for (my $t = $wcs->[0]; $t <= $wcs->[1]; $t += $ti) {
	    push(@$tickW, $t);
	}
    }

    my $tickS = $gdCanvas->worldToScreenArray($tickW);
    my $nTicks = scalar(@$tickS);
    my $tickL = $self->{tickLabel};
    my $font = $self->{font};

    for (my $t = 0;$t < $nTicks;++$t) {
	my $tW = $tickW->[$t];

	my $tS = $tickS->[$t];
	my $tS1 = int($tS - ($tw / 2));

	my $tick = new GD::Polygon;

	$tick->addPt($tS, $y1);
	$tick->addPt($tS + $tw - 1, $y1 + $ht);
	$tick->addPt($tS + $tw - 1, $y1 + $ht + $th);
	$tick->addPt($tS, $y1 + $ht + $th);

	$image->filledPolygon($tick, $color);

	my $label;

	if ((defined($tickLabels) && (scalar(@$tickLabels) == $nTicks))) {
	    $label = $tickLabels->[$t];
	} else {
	    $label = $tW . $tickL;
	}

	my $labelW = $font->width * length($label);

	# Center the label
	#
	$image->string($font,int($tS - ($labelW / 2)),$y1 + $ht + $th,$label,$color);
    }

    # Call superclass draw
    #
    $self->SUPER::drawSelf($gdCanvas);
}

# Automatically compute the positions of (and labels for) tick marks.
# This subroutine was ported from the bioWidget Java code, and was 
# originally written by Jonathan Schug, I believe.  It still needs
# some debugging, as there are some situations in which it will make
# bad decisions about what ticks/marks to place on the axis (e.g.,
# it might say "0kb  0kb  0kb  1kb  1kb  1kb", presumably due to 
# a rounding-related problem.)
#
sub autocomputeTicks {
    my($self, $wcs, $scs, $minGapChars) = @_;

    my $tickL = $self->{tickLabel};
    my $ticks = [];
    my $tickLabels = [];

    my $worldLen = $wcs->[1] - $wcs->[0] + 1;
    my $screenLen = $scs->[1] - $scs->[0] + 1;

    my $pixelsPerUnit = $screenLen / $worldLen;
    my $hasNegUnits = ($wcs->[0] < 0);
    my $a1 = abs($wcs->[0]); my $a2 = abs($wcs->[1]);
    my $absMax = ($a1 > $a2) ? $a1 : $a2;

    my $ptp = undef;

    if ($tickL eq 'aa') {
	$ptp = WDK::Model::GDUtil::PrecomputedTickParams->new($absMax, ['aa', 'k aa', 'M aa'], [], $hasNegUnits);
    } else {
	$ptp = WDK::Model::GDUtil::PrecomputedTickParams->new($absMax, ['bp', 'kb', 'Mb'], [], $hasNegUnits);
    }

    if ($DEBUG) {
	print STDERR "AxisSpan: leftDigits=", $ptp->{leftDigits},
	" unitsIndex=", $ptp->{unitsIndex},
	" unitFactor=", $ptp->{unitFactor}, "\n";
    }

    my $font = $self->{font};
    my $fw = $font->width;
    my $minGapPix = $fw * $minGapChars;

    my $params = &WDK::Model::GDUtil::TickParams::computeTickParams($ptp, $pixelsPerUnit, $fw, $minGapPix);

    if ($DEBUG) {
	print STDERR "AxisSpan: minGapPix=$minGapPix fontWidth=$fw pixelsPerUnit=$pixelsPerUnit\n";
	print STDERR "AxisSpan: tickDistance = ", $params->{tickDistance}, "\n";
	print STDERR "AxisSpan: precision = ", $params->getPrecision(), "\n";
	print STDERR "AxisSpan: precisionFactor = ", $params->{precisionFactor}, "\n";
	print STDERR "AxisSpan: rightDigits = ", $params->{rightDigits}, "\n";
	print STDERR "AxisSpan: totalDigits = ", $params->getTotalDigits(), "\n";
    }
    
    # compute the first tick
    my $tick = $wcs->[0];
    
    # JC:
    # Figure out some way to make the tickDistance more reasonable.
    # e.g., take the current scale into account when computing the tickDistance,
    # so that one tick doesn't appear after 4kb, the next after 5kb, etc.

    while ($tick < $wcs->[1]) {
	
#		// get tick label
#		String tickString = params.getTickString(tick);
#		int string_width = fontm.stringWidth(tickString);
#		int string_x = tick_x - string_width/2;
#
#		// draw if within clipbox
#		if (tick_x >= start_view && tick_x < end_view) {
#		    g.fillRect(tick_x - 1, tick_y, 2, tickHeight);
#
#		    if (labelSide.isForward())
#			g.drawString(tickString, string_x, tick_y - font_descent);
#		    else 
#			g.drawString(tickString, string_x, tick_y + tickHeight + font_ascent);
#		}
#        

	my $tickString = $params->getTickString($tick);

	push(@$ticks, $tick);
	push(@$tickLabels, $tickString);
	$tick += $params->{tickDistance};
    }

    return ($ticks, $tickLabels);
}
