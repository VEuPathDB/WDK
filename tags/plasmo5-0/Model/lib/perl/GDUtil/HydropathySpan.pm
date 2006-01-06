#!/usr/bin/perl

#------------------------------------------------------------------------
# HydropathySpan.pm
#
# A subclass of StripeSpan that displays a Kyte-Doolittle hydropathy
# plot for a protein sequence.
#
# Jonathan Crabtree
# 
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::HydropathySpan;

use strict;

use GD;
use WDK::Model::GDUtil::StripeSpan;

@WDK::Model::GDUtil::HydropathySpan::ISA = ('WDK::Model::GDUtil::StripeSpan');

#-------------------------------------------------
# Defaults
#-------------------------------------------------

my $DFLT_Y = 0;
my $DFLT_HEIGHT = 10;
my $DFLT_VERT_OFFSET = 0;
my $DFLT_WINDOW_SIZE = 19;  # see comments below

#-------------------------------------------------
# HydropathySpan
#-------------------------------------------------

sub new {
    my($class, $span) = @_;

    $span->{'label'} = 'Sequence' if (!defined($span->{'label'}));;
    $span->{'labelVAlign'} = 'center' if (!defined($span->{'labelVAlign'}));;
    $span->{'imagemapArea'} = 'all';

    my $self = $class->SUPER::new($span);

    my $sws = $span->{'windowSize'};

    $self->{'sequence'} = $span->{'aaseq'};
    $self->{'windowSize'} = defined($sws) ? $sws : $DFLT_WINDOW_SIZE;
    
    bless $self, $class;
    return $self;
}

sub getSelfHeight {
    my($self) = @_;
    return $self->{'height'};
}

sub drawSelf {
    my($self, $gdCanvas) = @_;
    $self->SUPER::drawSelf($gdCanvas);

    my $windowSize = $self->{windowSize};
    my $img = $gdCanvas->getImage();
    my $color = $img->colorExact(0,0,0);
    my $grey = $img->colorExact(153,153,153);

    my $seq = $self->{'sequence'};
    my $seqLen = length($seq);

    my $y1 = $self->{y1} - $self->getSelfHeight();
    my $ht = $self->getSelfHeight();
    my $voffset = $self->{vertOffset};

    my $end = $seqLen;

    my $cs = [$self->{x1}, $self->{x2}];
    my $scsAll = $gdCanvas->worldToScreenArray($cs);

    my $sum = 0;
    my $xvals = [];
    my $scores = [];

    # HACK - may change depending on scoring scheme
    #
    my $maxScore = 4.5;
    my $minScore = -4.5;
    
    for (my $i = 0; $i < $end;++$i) {
	my $inChar = substr($seq, $i, 1);
	my $inScore = &getKyteDoolittleScore($inChar);
	
	if ($i <= $windowSize) {
	    $sum += $inScore;
	} else {
	    my $outChar = substr($seq, $i - $windowSize, 1);
	    my $outScore = &getKyteDoolittleScore($outChar);
	    $sum += $inScore;
	    $sum -= $outScore;
	}

	# Plot a point at ($i - $windowSize/2), $sum/$windowSize
	#
	if ($i >= $windowSize) {
	    my $score = $sum / $windowSize;
	    my $posn = $i - ($windowSize / 2);

	    push(@$xvals, $posn);
	    push(@$scores, $score);
	}
    }

    my $scs = $gdCanvas->worldToScreenArray($xvals);
    my $npts = scalar(@$xvals);
    my $scoreRange = $maxScore - $minScore;
    my $baseY = $y1 + $voffset;
    my $zeroY = (((-$minScore) / $scoreRange) * $ht) + $baseY;

    $img->line($scsAll->[0], $zeroY, $scsAll->[1], $zeroY, $grey);

#    print STDERR "scoreRange = $minScore-$maxScore  baseY = $baseY  zeroY = $zeroY\n";

    for (my $i = 1;$i < $npts;++$i) {
	my $x1 = $scs->[$i-1];
	my $x2 = $scs->[$i];

	my $score1 = $scores->[$i-1];
	my $score2 = $scores->[$i];

	my $y1 = $baseY + $ht - ((($score1 - $minScore) / $scoreRange) * $ht);
	my $y2 = $baseY + $ht - ((($score2 - $minScore) / $scoreRange) * $ht);
	
	$img->line($x1,$y1,$x2,$y2,$color);
    }
}

my $kdScore = {
    'I' => 4.5,
    'V' => 4.2,
    'L' => 3.8,
    'F' => 2.8,
    'C' => 2.5,
    'M' => 1.9,
    'A' => 1.8,
    'G' => -0.4,
    'T' => -0.7,
    'W' => -0.9,
    'S' => -0.8,
    'Y' => -1.3,
    'P' => -1.6,
    'H' => -3.2,
    'E' => -3.5,
    'Q' => -3.5,
    'D' => -3.5,
    'N' => -3.5,
    'K' => -3.9,
    'R' => -4.5
};

# "When looking for surface regions in a globular protein, a window size of 9 was 
# found to give the best results. Surface regions can be identified as peaks below 
# the mid line. When looking for a transmembrane region in a protein, a window size 
# of 19 is needed. Transmembrane regions are identified by peaks with scores greater 
# than 1.6 using a window size of 19."
# 
# <http://occawlonline.pearsoned.com/bookbind/pubbooks/bc_mcampbell_genomics_1/medialib/activities/kd/kyte-doolittle-background.htm>
#
sub getKyteDoolittleScore {
    my($aa) = @_;
    $aa =~ tr/a-z/A-Z/;
    return $kdScore->{$aa};
}

1;
