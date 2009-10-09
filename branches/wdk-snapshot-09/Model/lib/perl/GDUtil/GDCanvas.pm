#!/usr/bin/perl

#------------------------------------------------------------------------
# GDCanvas.pm
#
# A wrapper for GD::Image that handles allocating a standard set of
# "web-safe" colors.  More importantly, it tracks both screen coordinates
# (e.g. pixels on the display) and world coordinates (e.g. bp in a 
# genomic sequence) and provides functions for converting between
# the two.
#
# Jonathan Crabtree
# 
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::GDCanvas;

use strict;
use GD;

sub new {
    my($class, $width, $height, $sc, $wc) = @_;

    my $image = new GD::Image($width, $height);
    my $white = $image->colorAllocate(255,255,255);
    my $black = $image->colorAllocate(0,0,0);

    my $self = {
	image => $image,
	width => $width,
	height => $height,
	fg_color => $black,
	bg_color => $white
	};

    bless $self, $class;

    $self->setScreenCoords($sc) if (defined $sc);
    $self->setWorldCoords($wc) if (defined $wc);

    return $self;
}

sub setScreenCoords {
    my($self, $c) = @_;
    $self->{screen_x1} = $c->{x1};
    $self->{screen_x2} = $c->{x2};
}

sub getScreenCoords { 
    my $self = shift;
    return {x1 => $self->{screen_x1}, x2 => $self->{screen_x2}};
}

sub setWorldCoords {
    my($self, $c) = @_;
    $self->{world_x1} = $c->{x1};
    $self->{world_x2} = $c->{x2};
}

sub getWorldCoords { 
    my $self = shift;
    return {x1 => $self->{world_x1}, x2 => $self->{world_x2}};
}

sub getImage {
    my $self = shift;
    return $self->{image};
}

sub getDefaultFgColor {
    my $self = shift;
    return $self->{fg_color};
}

sub getDefaultBgColor {
    my $self = shift;
    return $self->{bg_color};
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

# Convert a list (arrayref) of world coordinates to a 
# list of screen coordinates.
#
sub worldToScreenArray {
    my($self, $worldA) = @_;
    my $screenA = [];

    my $sx1 = $self->{screen_x1};
    my $sx2 = $self->{screen_x2};
    my $sw = $sx2 - $sx1;

    my $wx1 = $self->{world_x1};
    my $wx2 = $self->{world_x2};
    my $ww = $wx2 - $wx1;

    # Avoid division by zero:
    #
    if (($sw == 0) || ($ww == 0)) {
	foreach my $wc (@$worldA) {
	    push(@$screenA, $sx1);
	}
	return $screenA;
    }

    foreach my $wc (@$worldA) {
	my $sc = $sx1 + ((($wc - $wx1) / $ww) * $sw);
	push(@$screenA, $sc);
    }
    return $screenA;
}

1;
