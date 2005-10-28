#!/usr/bin/perl

#------------------------------------------------------------------------
# Packer.pm
#
# A packer is a subroutine that determines the vertical positions of 
# spans in a display (their horizontal positions are determined by
# their coordinates.)  There are several defined in this package.
#
# Jonathan Crabtree
# 
# $Revision$ $Date$ $Author$
#------------------------------------------------------------------------

package WDK::Model::GDUtil::Packer;

use strict;

#------------------------------------------------------------------------
# simplePacker
#
# Packs spans in the order they are encountered, each on
# a separate line with $yGap pixels betwixt them.
#
sub simplePacker {
    my($yGap) = @_;

    return sub {
	my($parentY, $kids) = @_;

	my $offset = $parentY - $yGap;
	my $height = $yGap;

	foreach my $kid (@$kids) {
#	    print STDERR "SimplePacker: placing kid at $offset\n";
	    $kid->{y1} = $offset;
	    $kid->pack();
	    my $ht = $kid->getHeight();
	    $offset -= ($ht + $yGap);
	    $height += ($ht + $yGap);
	}

	return $height;
    };
}

#------------------------------------------------------------------------
# constantPacker
#
# Packs all spans at the same y-value, relative
# to the parent.
#
sub constantPacker {
    my($yOffset) = @_;

    return sub {
	my($parentY, $kids) = @_;

	my $y = $parentY - $yOffset;
	my $height = 0;

	foreach my $kid (@$kids) {
#	    print STDERR "ConstantPacker: placing kid at $y\n";
	    $kid->{y1} = $y;
	    $kid->pack();
	    my $kh = $kid->getHeight();
	    $height = $kh if ($kh > $height);
	}
	return $height + $yOffset;
    };
}

#------------------------------------------------------------------------
# leftToRightPacker
#
# Sort spans by increasing x-coordinate and put as
# many non-overlapping spans on each line as will fit.
#
sub leftToRightPacker {
    my($yGap) = @_;

    return sub {
	my($parentY, $kids) = @_;

	my $offset = $parentY - $yGap;
	my $height = $yGap;

	my @sorted = sort { $a->{x1} <=> $b->{x1} } @$kids;
	my $numToPack = scalar(@sorted);
	my $numPacked = 0;
	my $rownum = 1;

	while ($numPacked < $numToPack) {
	    my $lastX2 = undef;
	    my $maxHt = 0;
	    my @leftToSort = ();
	
	    foreach my $kid (@sorted) {
		if (defined($lastX2)) {
		    if ($kid->{x1} > $lastX2) {
			
			# We have space - pack the kid here
			#
			$kid->{y1} = $offset;
			$kid->pack();
			my $ht = $kid->getHeight();
			$maxHt = $ht if ($ht > $maxHt);
			++$numPacked;
			$lastX2 = $kid->{x2};
		    } else {
			push(@leftToSort, $kid);
		    }
		} else {

		    # Pack as the first on this row
		    #
		    $kid->{y1} = $offset;
		    $kid->pack();
		    my $ht = $kid->getHeight();
		    $maxHt = $ht if ($ht > $maxHt);
		    ++$numPacked;
		    $lastX2 = $kid->{x2};
		}
	    }

	    # End of row

	    ++$rownum;
	    $offset -= ($maxHt + $yGap);
	    $height += ($maxHt + $yGap);
	    @sorted = @leftToSort;
	}

	return $height;
    };
}

#------------------------------------------------------------------------
# linePacker
#
# Discretizes the available horizontal space and
# packs spans as tightly as possible.  Works best 
# if all the spans to be packed are the same 
# height.
#
# $numCols   Resolution of the horizontal discretization.
#
sub linePacker {
    my($yGap, $numCols, $reverseStrand) = @_;
    $numCols = 100 if (!defined($numCols));

    return sub {
	my($parentY, $kids) = @_;
	my $offset = $parentY - $yGap;
	my $height = $yGap;
	my $numToPack = scalar(@$kids);
	my $numPacked = 0;

	# The discretized x2 coordinate (or "end column") of each kid
	#
	my $endCols = [];
	
	# Each entry in this array of size $numCols lists the indexes of
	# the kids whose x1 position falls into that column.
	#
	my $columns = [];

	for (my $i = 0;$i < $numCols; ++$i) {
	    $columns->[$i] = [];
	}

	# Scan kids to determine min and max x-coords
	#
	my $xmin = undef;
	my $xmax = undef;

	for (my $k = 0;$k < $numToPack;++$k) {
	    my $kid = $kids->[$k];

	    if (!defined($xmin)) {
		$xmin = $kid->{x1};
		$xmax = $kid->{x2};
	    } else {
		$xmin = $kid->{x1} if ($kid->{x1} < $xmin);
		$xmax = $kid->{x2} if ($kid->{x2} > $xmax);
	    }
	}

	my $columnWidth = ($xmax - $xmin) / $numCols;

	# Second scan to initialize discretized column values
	#
	for (my $k = 0;$k < $numToPack;++$k) {
	    my $kid = $kids->[$k];

	    # Discretize x1 and x2 values
	    #
	    my $x1col = int(($kid->{x1} - $xmin) / $columnWidth);
	    my $x2col = int(($kid->{x2} - $xmin) / $columnWidth);

	    # DEBUG
#	    print STDERR "Packer: numCols=$numCols coords=$x1col-$x2col\n";

	    $endCols->[$k] = $x2col;
	    push(@{$columns->[$x1col]}, $k);
	}

	# scan the bins of packed fragments (columns) from left to right,
	# placing the first available fragment and then jumping to its
	# ending column and continuing the scan until the end of the line.
	#
	my $row = 0;
	
	while($numPacked < $numToPack) {
	    ++$row;

#	    print STDERR "linePacker: row = $row\n";

	    my $col = 0;
	    my $maxRowHeight = undef;

	    while ($col < ($numCols + 1)) {
		my $kidnums = $columns->[$col];
		my $nkids = defined($kidnums) ? scalar(@$kidnums) : 0;

		if ($nkids == 0) {
		    ++$col;
		} else {

		    # We have a candidate; remove it from the list
		    #
		    my $knum = shift @$kidnums;

		    # Pack kid $knum on row $row
		    #
		    my $kid = $kids->[$knum];
		    $kid->{y1} = $offset;
		    $kid->pack();
		    my $kh = $kid->getHeight();
		    
		    if (defined($maxRowHeight)) {
			$maxRowHeight = $kh if ($kh > $maxRowHeight);
		    } else {
			$maxRowHeight = $kh;
		    }

		    # Jump to column after $kid's end column
		    #
		    $col = $endCols->[$knum] + 1;
		    ++$numPacked;

#		    print STDERR "linePacker: numPacked = $numPacked\n";
		}
	    }

	    $offset -= ($maxRowHeight + $yGap);
	    $height += ($maxRowHeight + $yGap);
	}

	# Reverse y-coordinates for the reverse strand
	#
	if ($reverseStrand) {
	    my $tp = (2 * $parentY) - $height + (4 * $yGap);
	    for (my $k = 0;$k < $numToPack;++$k) {
		my $kid = $kids->[$k];
		my $ky1 = $kid->{y1};
		$kid->{y1} = $tp - $ky1;
		$kid->pack();
	    }
	}

	return $height;
    };
}

1;
