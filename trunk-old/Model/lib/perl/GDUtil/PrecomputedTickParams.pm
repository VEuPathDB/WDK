#!/usr/bin/perl

#------------------------------------------------------------------------
# PrecomputedTickParams.pm
#
# Information on the AxisSpan's tick marks that can be precomputed.
# Ported from the bioWidget Java code.
#
# Created: Mon Nov 26 16:22:23 EST 2001
#
# Jonathan Crabtree
#
# $Revision$ $Date$ $Author$ 
#------------------------------------------------------------------------

package WDK::Model::GDUtil::PrecomputedTickParams;

use strict;

use WDK::Model::GDUtil::TickParams;

#-------------------------------------------------
# PrecomputedTickParams
#-------------------------------------------------

sub new {
    my($class, 
       $absRangeMax,      # largest absolute value represented in the coordinate system
       $posUnits,         # arrayref of labels for each power of 10**3 (e.g. "bp", "kb")
       $negUnits,         # arrayref of labels for each power of 10**-3 (e.g. "milli", "micro")
       $hasNegValues      # whether the coordinate system has any negative values
       ) = @_;

    my $self = {
	absRangeMax => $absRangeMax,
	posUnits => $posUnits,
	negUnits => $negUnits,
	hasNegValues => $hasNegValues,
    };
    bless $self, $class;

    # Compute leftDigits, UnitFactor, and UnitsIndex
    #
    my $nNeg = scalar(@$negUnits);
    my $nPos = scalar(@$posUnits);

    my $rangeOrderOfMag = &WDK::Model::GDUtil::TickParams::_integerLog($absRangeMax);
    my $leftDigits = $rangeOrderOfMag + 1;

    my $unitsIndex;

    my $maxLabelLen = 0;
    if ($absRangeMax >= 2.0) {
	my $rom3 = $rangeOrderOfMag / 3;
	$unitsIndex = int(($nPos < $rom3) ? $nPos : $rom3);
	$leftDigits = $leftDigits - 3 * $unitsIndex ;
    } else {
	# Maybe should be (-rangeOrderOfMag-2)?
	my $rom3 = (-$rangeOrderOfMag - 1) / 3 + 1;
	$unitsIndex = int(($nNeg < $rom3) ? $nNeg : $rom3);
	$leftDigits = $leftDigits - 3 * $unitsIndex;
    }
    $leftDigits = ($leftDigits > 1) ? $leftDigits : 1;   # at least one left digit
    my $unitFactor = 10.0 ** ($unitsIndex * 3);

    $self->setLeftDigits($leftDigits);
    $self->setUnitFactor($unitFactor);
    $self->setUnitsIndex($unitsIndex);

    return $self;
}

# Maximum number of digits to the left of the decimal point
#
sub setLeftDigits {
    my($self, $ld) = @_;
    $self->{leftDigits} = $ld;
}

sub getLeftDigits() { 
    my $self = shift;
    return $self->{leftDigits}; 
}

# The (actual) power of 10 that unitsIndex represents
#
sub setUnitFactor {
    my($self, $uf) = @_;
    $self->{unitFactor} = $uf;
}

sub getUnitFactor() { 
    my $self = shift;
    return $self->{unitFactor}; 
}

# which unit label to use (e.g. 'bp', 'kb', 'Mb')
#
sub setUnitsIndex {
    my($self, $ui) = @_;
    $self->{unitsIndex} = $ui;
}

sub getUnitsIndex() { 
    my $self = shift;
    return $self->{unitsIndex}; 
}

sub getUnitString {
    my($self) = @_;
    my $unitsIndex = $self->{unitsIndex};

    if ($unitsIndex < 0) {
	return $self->{negUnits}->[-$unitsIndex - 1];
    } else {
	return $self->{posUnits}->[$unitsIndex];
    }
}

1;
