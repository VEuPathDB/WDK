#!/usr/bin/perl

#------------------------------------------------------------------------
# VertLineSpan.pm
#
# A span that draws itself as a vertical line on the display.  It
# ignores its x2 coordinate and instead uses a user-supplied 'width' 
# value to determine its width.  It's handy for indicating the
# location of a discontinuity (gap) in an assembly genomic sequence,
# for example.
#
# Jonathan Crabtree
# 
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::VertLineSpan;

use strict;

use GD;
use WDK::Model::GDUtil::Span;
@WDK::Model::GDUtil::VertLineSpan::ISA = ('WDK::Model::GDUtil::Span');

#-------------------------------------------------
# Defaults
#-------------------------------------------------

my $DFLT_WIDTH = 2;
my $DFLT_BORDER_OFFSET = 2;

#-------------------------------------------------
# VertLineSpan
#-------------------------------------------------

sub new {
    my($class, $args) = @_;
    my $self = $class->SUPER::new($args);
    bless($self, $class);

    my $width = $args->{width};
    my $borderOffset = $args->{borderOffset};
    my $fgColor = $args->{fgColor};

    $self->{width} = defined($width) ? $width : $DFLT_WIDTH;
    $self->{borderOffset} = defined($borderOffset) ? $borderOffset : $DFLT_BORDER_OFFSET;
    $self->{fgColor} = $fgColor if defined($fgColor);

    return $self;
}

#-------------------------------------------------
# Span
#-------------------------------------------------

sub getSelfHeight {
    my($self) = @_;
    return 0;         # bending the truth
}

sub drawSelf {
    my($self, $gdCanvas) = @_;
    my $image = $gdCanvas->getImage();
    my $width = $self->{width};
    my $bOffset = $self->{borderOffset};
    my $color = $self->{color};
    my $fgColor = $self->{fgColor};

    $fgColor = $gdCanvas->getDefaultFgColor() if (!defined($fgColor));
    $color = $fgColor if (!defined($color));

    my $cs = [$self->{x1}, $self->{x2}];
    my $scs = $gdCanvas->worldToScreenArray($cs);

    my $rect = new GD::Polygon;

    my $sx1 = $scs->[0] - int($width/2);
    my $sx2 = $sx1 + $width;
    my $top = $gdCanvas->{height} - ($bOffset * 2 + 1);

    $rect->addPt($sx1, $bOffset);
    $rect->addPt($sx2, $bOffset);
    $rect->addPt($sx2, $top);
    $rect->addPt($sx1, $top);

    # draw a colored area of the desired width
    #
    $image->filledPolygon($rect, $color);

    # and then a single vertical line on either side (if width > 1)
    #
    $image->line($sx1,$bOffset,$sx1,$top,$fgColor);
    $image->line($sx2,$bOffset,$sx2,$top,$fgColor);
#    $image->dashedLine($sx1,$bOffset,$sx1,$top,$fgColor);
#    $image->dashedLine($sx2,$bOffset,$sx2,$top,$fgColor);
}
