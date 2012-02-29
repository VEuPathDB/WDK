#!/usr/bin/perl

#------------------------------------------------------------------------
# TickParams.pm
#
# Based on the Java class cbil.bioWidgets.map.shape.TickParams; original 
# code by Jonathan Schug.  Computes the correct label and precision to 
# use in labeling a sequence axis (e.g. when to use 2.3 kb vs. 2300 bp)
#
# Created: Mon Nov 26 12:31:01 EST 2001
#
# Jonathan Crabtree
#
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::TickParams;

use strict;

use POSIX;

#-------------------------------------------------
# TickParams
#-------------------------------------------------

sub new {
    my($class, $tickDist, $precomputedParams) = @_;

    my $self = {
	tickDistance => $tickDist,                       # distance between successive ticks in world coordinates
	precisionFactor => undef,                        # 10 exp rightDigits
	rightDigits => undef,                            # number of digits to right of decimal point
	leftDigits => $precomputedParams->{leftDigits},
	precomputedParams => $precomputedParams
    };

    bless($self, $class);
    return $self;
}

sub setTickDistance {
    my($self, $td) = @_;
    $self->{tickDistance} = $td;
}

sub getTotalDigits {
    my($self) = @_;
    my $sign = $self->{precomputedParams}->{hasNegValues} ? 1 : 0;
    my $rightDigits = $self->{rightDigits};
    my $period = ($rightDigits == 0) ? 0 : 1;
    return $sign + $period + $rightDigits + $self->{precomputedParams}->{leftDigits}; 
}

sub getPrecision {
    my($self) = @_;
    my $distanceInUnits = $self->{tickDistance} / $self->{precomputedParams}->{unitFactor};
    my $disUnitsOrderOfMag = &_integerLog($distanceInUnits);
    $self->{rightDigits} = (0 > -$disUnitsOrderOfMag) ? 0 : -$disUnitsOrderOfMag;
    $self->{precisionFactor} = 10 ** $self->{rightDigits};
}

sub quantize {
    my($self) = @_;
    my $tickDistance = $self->{tickDistance};
    my $pow10 = 10.0 ** &_integerLog($tickDistance);
    my $d_quant = ($tickDistance / $pow10);  # normalize to 1 - 10
    # the cube root of 10 is 2.15, 2.15 squared is 4.64 -- doing a ceiling not floor
    my $quant = ($d_quant >= 4.64) ? 10.0 : ($d_quant >= 2.15 ? 4.64 : 2.15);
    $self->{tickDistance} = $quant * $pow10;     
}

sub getTickString {
    my($self, $tick) = @_;
    my $precomp = $self->{precomputedParams};
    my $precisionFactor = $self->{precisionFactor};

    my $tickVal = $tick / $precomp->{unitFactor};

    if ($self->{rightDigits} == 0) {  # no ".0"
        return "" . &_round($tickVal) . $precomp->getUnitString();
    }

    $tickVal = &_round($tickVal * $precisionFactor) / ($precisionFactor * 1.0);
    return "" . $tickVal + $precomp->getUnitString();
}

#-------------------------------------------------
# Package methods
#-------------------------------------------------

# $pixelsPerUnit  Number of pixels per unit world coordinate
# $precomp        PrecomputedTickParams
# $charWidth      Maximum width of a character in the font used to render the labels
# $minGapPix      Minimum number of pixels between successive labels
#
sub computeTickParams {
    my($precomp, $pixelsPerUnit, $charWidth, $minGapPix) = @_;

    # determine number of right digits
    # .......... 1 .......... 
    # assume none needed and refine.
    # total number of digits, -L.R
    my $rightDigits = 0;
    my $totalDigits = $precomp->{leftDigits} + 1 + $rightDigits;
    ++$totalDigits if ($precomp->{hasNegValues}); # sign
    my $unitStringSize = length($precomp->getUnitString()) * $charWidth;

    my $distancePixels = $charWidth * $totalDigits + $unitStringSize + $minGapPix;
    my $tickParams = WDK::Model::GDUtil::TickParams->new($distancePixels / $pixelsPerUnit, $precomp);
	
    # .......... 2 .......... 
    # choose better number of right digits
    $distancePixels = int(($charWidth * $tickParams->getTotalDigits() + 
			   $unitStringSize + $minGapPix));

    $tickParams->setTickDistance($distancePixels / $pixelsPerUnit);
    $tickParams->quantize(); # Quantization
    return $tickParams;
}

my $LN10 = log(10);

sub _integerLog {
    my $value = shift;
    return int(POSIX::floor(log($value) / $LN10));
}

sub _round {
    my $value = shift;
    return sprintf("%d", $value)
}
