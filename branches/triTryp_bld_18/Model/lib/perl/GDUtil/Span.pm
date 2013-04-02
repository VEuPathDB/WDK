#!/usr/bin/perl

#------------------------------------------------------------------------
# Span.pm
#
# A Span is basically a rectangle; it has a start coordinate and an
# end coordinate, it may be visible or invisible (depending on its
# shape) and it may have one or more child spans.  A Span with children
# will also have a packer that determines how this child spans are 
# arranged horizontal on the screen.
#
# Jonathan Crabtree
# 
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::Span;

use strict;

use GD;
use WDK::Model::GDUtil::Packer;

#-------------------------------------------------
# Defaults
#-------------------------------------------------

my $DFLT_Y = 0;
my $DFLT_HEIGHT = 10;
my $DFLT_PACKER = WDK::Model::GDUtil::Packer::simplePacker(5);
my $DFLT_SHAPE = 'rect';
my $DFLT_FILLED = 1;
my $DFLT_BORDER = 0;
my $DFLT_VERT_OFFSET = 0;

#-------------------------------------------------
# Span
#-------------------------------------------------

sub new {
    my($class, $span) = @_;

    my $self = {
	kids => [],
	y1 => defined($span->{y1}) ? $span->{y1} : $DFLT_Y,
	height => defined($span->{height}) ? $span->{height} : $DFLT_HEIGHT,
	packer => defined($span->{packer}) ? $span->{packer} : $DFLT_PACKER,
	shape => defined($span->{shape}) ? $span->{shape} : $DFLT_SHAPE,
	filled => defined($span->{filled}) ? $span->{filled} : $DFLT_FILLED,
	border => defined($span->{border}) ? $span->{border} : $DFLT_BORDER,
	vertOffset => defined($span->{vertOffset}) ? $span->{vertOffset} : $DFLT_VERT_OFFSET,
    };

    if ($span->{x1} > $span->{x2}) {
	$self->{x1} = $span->{x2};
	$self->{x2} = $span->{x1};
    } else {
	$self->{x1} = $span->{x1};
	$self->{x2} = $span->{x2};
    }

    bless $self, $class;

    $self->{color} = $span->{color} if (defined $span->{color});
    $self->{imagemapLabel} = $span->{imagemapLabel} if defined($span->{imagemapLabel});
    $self->{imagemapHref} = $span->{imagemapHref} if defined($span->{imagemapHref});
    $self->{imagemapTarget} = $span->{imagemapTarget} if defined($span->{imagemapTarget});
    $self->{imagemapOnMouseOver} = $span->{imagemapOnMouseOver} if defined($span->{imagemapOnMouseOver});
    $self->{imagemapOnMouseOut} = $span->{imagemapOnMouseOut} if defined($span->{imagemapOnMouseOut});

    if (defined($span->{kids})) {
	foreach my $kid (@{$span->{kids}}) {
	    $self->addKid($kid);
	}
    }

    return $self;
}

sub addKid {
    my($self, $kid) = @_;
    push(@{$self->{kids}}, $kid);
}

sub getKids {
    my $self = shift;
    return $self->{kids};
}

sub translateY {
    my($self, $deltaY) = @_;
    $self->{y1} += $deltaY;
    $self->translateKidsY($deltaY);
}

sub translateKidsY {
    my($self, $deltaY) = @_;
    foreach my $kid ($self->{kids}) {
	$kid->translateY($deltaY);
    }
}

sub pack {
    my($self) = @_;
    my $packer = $self->{packer};

#    print STDERR "Span.pm: $self packing kids at ", $self->{y1}, "\n";

    $self->{kidsHeight} = &$packer($self->{y1} - $self->getSelfHeight(), $self->{kids});
}

sub getHeight {
    my($self) = @_;
    if (scalar(@{$self->{kids}}) > 0) {
	return $self->{kidsHeight} + $self->getSelfHeight();
    }
    return $self->getSelfHeight();
}

sub draw {
    my($self, $gdCanvas) = @_;
    $self->drawSelf($gdCanvas);

    foreach my $kid (@{$self->{kids}}) {
	$kid->draw($gdCanvas);
    }
}

sub makeImageMap {
    my($self, $gdCanvas) = @_;
    my $im = '';

    my $shape = $self->{shape};
    my $label = $self->{imagemapLabel};
    my $href = $self->{imagemapHref};
    my $target = $self->{imagemapTarget};
    my $onMouseOver = $self->{imagemapOnMouseOver};
    my $onMouseOut = $self->{imagemapOnMouseOut};

    if ($label || $href || $onMouseOver || $onMouseOut) {

	# Get bounding box, convert coordinates.
	#
	my $bbox = $self->getBBox($gdCanvas);

	my $coords = [$bbox->{x}, 
		      $bbox->{y} - $bbox->{height},
		      $bbox->{x} + $bbox->{width},
		      $bbox->{y}
		      ];

	$im .= "  <AREA SHAPE=\"rect\" COORDS=\"" . join(',', @$coords) . "\" ";

	if (defined($href)) {
	    $im .= "HREF=\"$href\" ";
	} else {
	    $im .= "NOHREF ";
	}

	$im .= "TARGET=\"$target\" " if ($target);
	$im .= "TITLE=\"$label\" " if ($label);

	if ($onMouseOver || $onMouseOut) {
	    $im .= "ONMOUSEOVER=\"${onMouseOver}\" " if $onMouseOver;
	    $im .= "ONMOUSEOUT=\"${onMouseOut}\" " if $onMouseOut;
	} 
	elsif ($label) {
	    $im .= "ONMOUSEOVER=\"window.status='$label'; return true;\" ";
	    $im .= "ONMOUSEOUT=\"window.status=''; return true;\" ";
	}

	$im .= ">\n";
    }

    foreach my $kid (@{$self->{kids}}) {
	$im .= $kid->makeImageMap($gdCanvas);
    }

    return $im;
}

sub getBBox {
    my($self, $gdCanvas) = @_;
    my $cs = [$self->{x1}, $self->{x2}];
    my $wcs = $gdCanvas->worldToScreenArray($cs);
    my $width = int($wcs->[1] - $wcs->[0]);
    # everything is at least 1 pixel wide so that mouseover will always work
    $width = $width < 1 ? 1 : $width; 

    return {x => int($wcs->[0]),
	    y => $self->{y1},
	    width => $width, 
	    height => $self->getHeight()};
}

#-------------------------------------------------
# Methods designed to be overidden in subclasses
#-------------------------------------------------

sub getSelfBBox {
    my($self, $gdCanvas) = @_;
    my $cs = [$self->{x1}, $self->{x2}];
    my $wcs = $gdCanvas->worldToScreenArray($cs);

    return {x => int($wcs->[0]),
	    y => $self->{y1} - $self->{vertOffset} - $self->getSelfHeight(),
	    width => int($wcs->[1] - $wcs->[0]), 
	    height => $self->getSelfHeight()};
}

sub getSelfHeight {
    my($self) = @_;
    return $self->{height};
}

# Default is to draw a rectangle.
#
sub drawSelf {
    my($self, $gdCanvas) = @_;
    my $cs = [$self->{x1}, $self->{x2}];
    my $y1 = $self->{y1} - $self->getSelfHeight();
    my $voffset = $self->{vertOffset};

    my $scs = $gdCanvas->worldToScreenArray($cs);
    my $image = $gdCanvas->getImage();
    my $color = $self->{color};
    my $fgColor = $gdCanvas->getDefaultFgColor();
    $color = $fgColor if (!defined($color));
    
#    print STDERR "Span: drawing from ", $cs->[0], " to ", $cs->[1], "\n";
#    print STDERR "Span: drawing from ", $scs->[0], " to ", $scs->[1], "\n";
#    print STDERR "Span: y1 = ", $y1, " height = ", $self->{height}, "\n";

    my $ht = $self->getSelfHeight();
    my $shape = $self->{shape};

    if ($shape eq 'rect') {
	my $rect = new GD::Polygon;
	$rect->addPt($scs->[0], $y1 + $voffset);
	$rect->addPt($scs->[1], $y1 + $voffset);
	$rect->addPt($scs->[1], $y1 + $ht + $voffset);
	$rect->addPt($scs->[0], $y1 + $ht + $voffset);

	if ($self->{filled}) {
	    $image->filledPolygon($rect, $color);
	} else {
	    $image->polygon($rect, $color);
	}
	$image->polygon($rect, $fgColor) if ($self->{border});
    } 

    # A pointy rectangle pointing to the left
    #
    elsif ($shape eq 'reverse') {
	my $poly = new GD::Polygon;
	my $swh = int(($scs->[1] - $scs->[0]) / 2.0);
	my $sc = $scs->[0] + (($swh < $ht) ? $swh : $ht);

	$poly->addPt($sc, $y1 + $voffset);
	$poly->addPt($scs->[0], int($y1 + $ht / 2.0) + $voffset);
	$poly->addPt($sc, $y1 + $ht + $voffset);
	$poly->addPt($scs->[1], $y1 + $ht + $voffset);
	$poly->addPt($scs->[1], $y1 + $voffset);

	if ($self->{filled}) {
	    $image->filledPolygon($poly, $color);
	} else {
	    $image->polygon($poly, $color);
	}
	$image->polygon($poly, $fgColor) if ($self->{border});
    } 

    # A pointy rectangle pointing to the right
    #
    elsif ($shape eq 'forward') {
	my $poly = new GD::Polygon;
	my $swh = int(($scs->[1] - $scs->[0]) / 2.0);
	my $sc = $scs->[1] - (($swh < $ht) ? $swh : $ht);

	$poly->addPt($sc, $y1 + $voffset);
	$poly->addPt($scs->[1], int($y1 + $ht / 2.0) + $voffset);
	$poly->addPt($sc, $y1 + $ht + $voffset);
	$poly->addPt($scs->[0], $y1 + $ht + $voffset);
	$poly->addPt($scs->[0], $y1 + $voffset);

	if ($self->{filled}) {
	    $image->filledPolygon($poly, $color);
	} else {
	    $image->polygon($poly, $color);
	}
	$image->polygon($poly, $fgColor) if ($self->{border});
    }
}

1;
