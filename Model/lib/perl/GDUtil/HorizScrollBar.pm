#!/usr/bin/perl

#------------------------------------------------------------------------
# HorizScrollBar.pm
#
# A simple implementation of a horizontal "scrollbar" to be used in 
# CGI web-based applications.  It simply draws a static image of a
# scrollbar, indicating the currently-viewed region, and allows the
# user to click on either end of the scrollbar (which will presumably
# be hyperlinked to the next page(s).)
#
# Jonathan Crabtree
# 
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::HorizScrollBar;

use strict;
use GD;

#-------------------------------------------------
# Defaults
#-------------------------------------------------

my $DFLT_IMG_WIDTH = 600;
my $DFLT_IMG_HEIGHT = 30;
my $DFLT_ARROW_MARGIN = 2;
my $DFLT_WINDOW_MARGIN = 5;

#-------------------------------------------------
# HorizScrollBar
#-------------------------------------------------

sub new {
    my($class, $args) = @_;

    my $iw = defined($args->{imgWidth}) ? $args->{imgWidth} : $DFLT_IMG_WIDTH;
    my $ih = defined($args->{imgHeight}) ? $args->{imgHeight} : $DFLT_IMG_HEIGHT;
    my $a1 = $args->{axis_x1};
    my $a2 = $args->{axis_x2};
    my $w1 = $args->{window_x1};
    my $w2 = $args->{window_x2};

    my $self = {
	# margin around the arrows
	arrowMargin => defined($args->{arrow_margin}) ? $args->{arrow_margin} : $DFLT_ARROW_MARGIN,
	# margin above and below the window icon
	windowMargin => defined($args->{window_margin}) ? $args->{window_margin} : $DFLT_WINDOW_MARGIN,
    };
    bless $self, $class;

    $self->setImageSize($iw, $ih);
    $self->setAxisCoords($a1, $a2);
    $self->setWindowCoords($w1, $w2);

    return $self;
}

sub setImageSize {
    my($self, $width, $height) = @_;

    $self->{width} = $width;
    $self->{height} = $height;
    my $image = new GD::Image($width, $height);
    $self->{image} = $image;
    $self->{white} = $image->colorAllocate(255,255,255);
    $self->{black} = $image->colorAllocate(0,0,0);

    $self->allocateWebSafePalette();
}

# Sets the extrema of the entire scrollable region.
#
sub setAxisCoords {
    my($self, $x1, $x2) = @_;
    $self->{axis_x1} = $x1;
    $self->{axis_x2} = $x2;
}

# Sets the area of the axis currently in view
#
sub setWindowCoords {
    my($self, $x1, $x2) = @_;
    $self->{window_x1} = $x1;
    $self->{window_x2} = $x2;
}

# Allocate the 216 colors deemed "safe" for use on the Web.
# Does nothing unless there are 216 open color slots.
#
sub allocateWebSafePalette {
    my $self = shift;
    my $image = $self->{image};
    my $cVals = [0, 51, 102, 153, 204, 255];

    for my $r (@$cVals) {
	for my $g (@$cVals) {
	    for my $b (@$cVals) {
		$image->colorAllocate($r, $g, $b);
	    }
	}
    }
}

# Render the image
#
sub draw {
    my($self) = @_;
    my $image = $self->{image};
    my $width = $self->{width};
    my $height = $self->{height};
    my $aMargin = $self->{arrowMargin};
    my $wMargin = $self->{windowMargin};

    # Hard-coded color choices
    #
    my $arrowColor = $image->colorExact(51, 51, 102);
    my $axisColor = $image->colorExact(0, 0, 0);
    my $windowColor = $image->colorExact(153, 153, 255);

    my $halfHt = $height / 2;
    my $aWidth = ($height - ($aMargin * 2)) * 0.5;

    # Left arrow (move left)
    #
    my $la = new GD::Polygon;
    $la->addPt($aMargin, $halfHt);
    $la->addPt($aMargin + $aWidth, $aMargin);
    $la->addPt($aMargin + $aWidth, $height - $aMargin);
    $image->filledPolygon($la, $arrowColor);

    # Right arrow (move right)
    #
    my $ra = new GD::Polygon;
    $ra->addPt($width - $aMargin, $halfHt);
    $ra->addPt($width - ($aMargin + $aWidth), $aMargin);
    $ra->addPt($width - ($aMargin + $aWidth), $height - $aMargin);
    $image->filledPolygon($ra, $arrowColor);

    # Central axis
    #
    my $axisOffset = $aMargin * 2 + $aWidth;

    my $axis = new GD::Polygon;
    $axis->addPt($axisOffset + 1, $halfHt - 1);
    $axis->addPt($axisOffset + 1, $halfHt + 1);
    $axis->addPt($width - $axisOffset, $halfHt + 1);
    $axis->addPt($width - $axisOffset, $halfHt - 1);
    $image->filledPolygon($axis, $axisColor);

    # Rectangle representing current viewable area
    #

    # Compute $wx1 and $wx2 based on axis and window coordinates
    #
    my $ax1 = $self->{axis_x1}; my $ax2 = $self->{axis_x2};
    my $wx1 = $self->{window_x1}; my $wx2 = $self->{window_x2};

    my $axisWidth = abs($ax2 - $ax1);
    my $axisScreenWidth = $width - (2 * $axisOffset);
    
    my $wLeft = $axisOffset + ((($wx1 - $ax1) / $axisWidth) * $axisScreenWidth) + 1;
    my $wRight = $axisOffset + ((($wx2 - $ax1) / $axisWidth) * $axisScreenWidth);

    my $wTop = $height - $wMargin - 1;
    my $w = new GD::Polygon;
    $w->addPt($wLeft, $wMargin);
    $w->addPt($wRight, $wMargin);
    $w->addPt($wRight, $wTop);
    $w->addPt($wLeft, $wTop);
    $image->filledPolygon($w, $windowColor);

#    my $ptp = WDK::Model::GDUtil::PrecomputedTickParams->new($absMax, ['bp', 'kb', 'Mb'], [], $hasNegUnits);

}

# Determine where the X-component of a mouse click is located.
# The return value is one of the following:
#  1. 'left arrow'
#  2. 'right arrow'
#  3. <a number indicating the world coordinates of the click location>
#  4. undef for none of the above
#
sub getXLocation {
    my($self, $screenX) = @_;
    
    my $width = $self->{width};
    my $height = $self->{height};
    my $aMargin = $self->{arrowMargin};
    my $wMargin = $self->{windowMargin};
    my $ax1 = $self->{axis_x1}; 
    my $ax2 = $self->{axis_x2};

    my $aWidth = ($height - ($aMargin * 2)) * 0.5;
    my $axisOffset = $aMargin * 2 + $aWidth;

    my $leftEnd = $axisOffset;
    my $rightEnd = $width - $axisOffset;
    my $axisWidth = abs($ax2 - $ax1);
    my $axisScreenWidth = $width - (2 * $axisOffset);

    if ($screenX < $leftEnd) {
	return "left arrow";
    } elsif ($screenX > $rightEnd) {
	return "right arrow";
    } else {
	my $w = $ax1 + ((($screenX - $leftEnd) / $axisScreenWidth) * $axisWidth);
	return $w;
    }

    return undef;
}

sub getPng {
    my($self) = @_;
    $self->draw();
    my $img = $self->{image};
    return $img->can('png') ? $img->png() : $img->gif();
}

1;

