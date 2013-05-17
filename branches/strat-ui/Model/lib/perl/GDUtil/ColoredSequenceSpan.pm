#!/usr/bin/perl

#------------------------------------------------------------------------
# ColoredSequenceSpan.pm
#
# A subclass of StripeSpan that displays a colorful graphic that 
# reflects the amino acid or base composition of a sequence.
#
# Jonathan Crabtree
# 
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::ColoredSequenceSpan;

use strict;

use GD;
use WDK::Model::GDUtil::StripeSpan;

@WDK::Model::GDUtil::ColoredSequenceSpan::ISA = ('WDK::Model::GDUtil::StripeSpan');

#-------------------------------------------------
# Defaults
#-------------------------------------------------

my $DFLT_Y = 0;
my $DFLT_HEIGHT = 10;
my $DFLT_VERT_OFFSET = 0;

#-------------------------------------------------
# ColoredSequenceSpan
#-------------------------------------------------

sub new {
    my($class, $span) = @_;

    $span->{'label'} = 'Sequence' if (!defined($span->{'label'}));;
    $span->{'labelVAlign'} = 'center' if (!defined($span->{'labelVAlign'}));;
    $span->{'imagemapArea'} = 'all';

    my $self = $class->SUPER::new($span);

    if (defined($span->{'aaseq'})) {
	$self->{'sequence'} = $span->{'aaseq'};
	$self->{'is_dna'} = 0;
    } 
    elsif (defined($span->{'dnaseq'})) {
	$self->{'sequence'} = $span->{'aaseq'};
	$self->{'is_dna'} = 1;
    }

    bless $self, $class;
    return $self;
}

sub getColor {
    my($self, $img, $isDna, $char) = @_;
    
    if ($isDna) {                # DNA
	if ($char =~ /a/i) {
	    return $img->colorExact(247,174,0);
	} elsif ($char =~ /c/i) {
	    return $img->colorExact(0,0,205);
	} elsif ($char =~ /g/i) {
	    return $img->colorExact(255,0,255);
	} elsif ($char =~ /t/i) {
	    return $img->colorExact(255,0,0);
	} else {
	    return $img->colorExact(255,255,255);
	}
    } 

    # In case you were wondering, these colors were originally chosen to 
    # match those used by Martin Fraunholz in a similar set of protein
    # sequence displays that he generated for PlasmoDB.org.

    else {                       # AA 
	if ($char =~ /p/i) {
	    return $img->colorExact(255,255,255);
	} elsif ($char =~ /[mc]/i) {
	    return $img->colorExact(247,174,0);
	} elsif ($char =~ /[de]/i) {
	    return $img->colorExact(0,0,205);
	} elsif ($char =~ /[ilva]/i) {
	    return $img->colorExact(255,0,255);
	} elsif ($char =~ /[rkh]/i) {
	    return $img->colorExact(255,0,0);
	} elsif ($char =~ /[stg]/i) {
	    return $img->colorExact(0,255,0);
	} elsif ($char =~ /[fyw]/i) {
	    return $img->colorExact(0,0,0);
	} elsif ($char =~ /[nq]/i) {
	    return $img->colorExact(150,150,150);
	} else {
	    return $img->colorExact(255,255,255);
	}
    }
}

sub getSelfHeight {
    my($self) = @_;
    return $self->{'height'};
}

sub drawSelf {
    my($self, $gdCanvas) = @_;
    $self->SUPER::drawSelf($gdCanvas);

    my $img = $gdCanvas->getImage();
    my $isDna = $self->{'is_dna'};
    my $seq = $self->{'sequence'};
    my $seqLen = length($seq);

    my $y1 = $self->{y1} - $self->getSelfHeight();
    my $ht = $self->getSelfHeight();
    my $voffset = $self->{vertOffset};

    my $cs = [];
    for (my $i = 1;$i < $seqLen;++$i) { push(@$cs, $i); }
    my $scs = $gdCanvas->worldToScreenArray($cs);

    for (my $i = 1;$i < $seqLen-1;++$i) {
	my $x1 = int($scs->[$i-1]);
	my $x2 = int($scs->[$i]);
	
	# Get color of this base/residue
	#
	my $char = substr($seq, $i, 1);
	my $color = $self->getColor($img, $isDna, $char);

	# Paint it
	#
	my $rect = new GD::Polygon;
	$rect->addPt($x1, $y1 + $voffset);
	$rect->addPt($x2, $y1 + $voffset);
	$rect->addPt($x2, $y1 + $ht + $voffset);
	$rect->addPt($x1, $y1 + $ht + $voffset);
	$img->filledPolygon($rect, $color);
    }
}

1;
