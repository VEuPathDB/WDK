#!/usr/bin/perl

#------------------------------------------------------------------------
# PercentATSpan.pm
#
# A subclass of StripeSpan that displays a percent A+T plot for a 
# nucleotide sequence.  The code was essentially lifted from 
# HydropathySpan.pm and should be generalized to allow the plotting 
# of arbitrary sequence functions using a single package.
#
# Jonathan Crabtree
# 
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::PercentATSpan;

use strict;

use GD;
use WDK::Model::GDUtil::StripeSpan;

@WDK::Model::GDUtil::PercentATSpan::ISA = ('WDK::Model::GDUtil::StripeSpan');

#-------------------------------------------------
# Defaults
#-------------------------------------------------

my $DFLT_Y = 0;
my $DFLT_HEIGHT = 10;
my $DFLT_VERT_OFFSET = 0;
my $DFLT_WINDOW_SIZE = 19;  # see comments below

#-------------------------------------------------
# PercentATSpan
#-------------------------------------------------

sub new {
    my($class, $span) = @_;

    $span->{'label'} = 'Percent AT' if (!defined($span->{'label'}));;
    $span->{'labelVAlign'} = 'center' if (!defined($span->{'labelVAlign'}));;
    $span->{'imagemapArea'} = 'all';

    my $self = $class->SUPER::new($span);

    my $sws = $span->{'windowSize'};
    my $plotColor = $span->{'plotColor'};
    my $lineColor = $span->{'lineColor'};

    $self->{'sequence'} = $span->{'naseq'};
    $self->{'windowSize'} = defined($sws) ? $sws : $DFLT_WINDOW_SIZE;
    $self->{'plotColor'} = $plotColor if (defined($plotColor));
    $self->{'lineColor'} = $lineColor if (defined($lineColor));

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

    my $x1 = $self->{x1};
    my $windowSize = $self->{windowSize};
    my $img = $gdCanvas->getImage();
    my $lineColor = $self->{lineColor};
    my $plotColor = $self->{plotColor};

    $lineColor = $img->colorExact(204,204,204) if (!defined($lineColor));
    $plotColor = $img->colorExact(0,0,0) if (!defined($plotColor));

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
    my $maxScore = 1.0;
    my $minScore = 0;
    
    for (my $i = 0; $i < $end;++$i) {
	my $inChar = substr($seq, $i, 1);
	my $inScore = ($inChar =~ /[at]/i) ? 1 : 0;
	
	if ($i <= $windowSize) {
	    $sum += $inScore;
	} else {
	    my $outChar = substr($seq, $i - $windowSize, 1);
	    my $outScore = ($outChar =~ /[at]/i) ? 1 : 0;
	    $sum += $inScore;
	    $sum -= $outScore;
	}

	# Plot a point at ($i - $windowSize/2), $sum/$windowSize
	#
	if ($i >= $windowSize) {
	    my $score = $sum / $windowSize;
	    my $posn = $i - ($windowSize / 2) + $x1;

	    push(@$xvals, $posn);
	    push(@$scores, $score);
	}
    }

    my $scs = $gdCanvas->worldToScreenArray($xvals);
    my $npts = scalar(@$xvals);
    my $scoreRange = $maxScore - $minScore;
    my $baseY = $y1 + $voffset;
    my $zeroY = $baseY + $ht;              # highlight bottom of the scale
    my $halfY = $baseY + ($ht / 2);

    $img->line($scsAll->[0], $zeroY, $scsAll->[1], $zeroY, $lineColor);
    $img->dashedLine($scsAll->[0], $halfY, $scsAll->[1], $halfY, $lineColor);
    $img->line($scsAll->[0], $baseY, $scsAll->[1], $baseY, $lineColor);

#    print STDERR "scoreRange = $minScore-$maxScore  baseY = $baseY  zeroY = $zeroY\n";

    for (my $i = 1;$i < $npts;++$i) {
	my $x1 = $scs->[$i-1];
	my $x2 = $scs->[$i];

	my $score1 = $scores->[$i-1];
	my $score2 = $scores->[$i];

	my $y1 = $baseY + $ht - ((($score1 - $minScore) / $scoreRange) * $ht);
	my $y2 = $baseY + $ht - ((($score2 - $minScore) / $scoreRange) * $ht);
	
	$img->line($x1,$y1,$x2,$y2,$plotColor);

	# DEBUG
#	print STDERR "PercentATSpan: score1=$score1 score2=$score2 drawing $x1,$y1,$x2,$y2\n";
#	last if ($i > 10);
    }
}

1;
