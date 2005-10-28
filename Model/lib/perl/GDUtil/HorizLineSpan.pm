#!/usr/bin/perl

#------------------------------------------------------------------------
# HorizLineSpan.pm
#
# A subclass of Span whose only purpose is to draw a horizontal 
# dividing line across the display.  It can be handy for dividing
# the display into tiers (e.g., one for gene predictions, one for
# BLAST results, etc.)
#
# Jonathan Crabtree
# 
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::HorizLineSpan;

use strict;

use GD;
use WDK::Model::GDUtil::Span;
@WDK::Model::GDUtil::HorizLineSpan::ISA = ('WDK::Model::GDUtil::Span');

#-------------------------------------------------
# HorizLineSpan
#-------------------------------------------------

sub new {
    my($class, $span) = @_;
    my $self = $class->SUPER::new($span);
    bless($self, $class);
    return $self;
}

#-------------------------------------------------
# Span
#-------------------------------------------------

sub getSelfHeight {
    my($self) = @_;
    return $self->{height};
}

sub drawSelf {
    my($self, $gdCanvas) = @_;
    my $image = $gdCanvas->getImage();
    my $sc = $gdCanvas->getScreenCoords();
    my $ht = $self->{height};
    my $color = $self->{color};

    my $rect = new GD::Polygon;
    $rect->addPt(5, $self->{y1});
    $rect->addPt($sc->{x2}, $self->{y1});
    $rect->addPt(5, $self->{y1} + $ht);
    $rect->addPt($sc->{x2}, $self->{y1} + $ht);

    $image->polygon($rect, $color);
}
