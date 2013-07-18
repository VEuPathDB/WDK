#!/usr/bin/perl

#------------------------------------------------------------------------
# GUSSimilarityQueryBuilder.pm
#
# Builds up similarity spans for GUSGenomicSequenceTransducer.
#
# Y. Thomas Gan
#
# $Revision$ $Date$ $Author$
#---------------------------------------------------------------------

package WDK::Model::GDUtil::Transducer::GUSSimilarityQueryBuilder;

use strict;
use DBI;

use WDK::Model::GDUtil::Transducer::DEBUG;

#-------------------------------------------------
# GUSSimilarityQueryBuilder
#-------------------------------------------------

my $debug = 0;

sub new {
    my($class, $args) = @_;
    
    my $self = {
        dbh => $args->{dbh},
        naSeqId => $args->{naSeqId},
        start => $args->{start},
        end => $args->{end}, 
	coreDb => $args->{coreDb},
	dotsDb => $args->{dotsDb},
    };
 
    bless $self, $class;
    
    $self->{aaQuery} = "";
    $self->{naQueryBaseSubject} = "";
    $self->{naQueryBaseQuery} = "";
    $self->{naQueryOrsSubject} = [];
    $self->{naQueryOrsQuery} = [];
    $self->{taQuery} = "";
    $self->{tableIds} = $self->getTableIds(['ExternalAASequence', 'ExternalNASequence', 'TranslatedAASequence']);
    return $self;
}

sub buildAAQuery {
    my ($self, $maxPValExp) = @_;

    my $dotsDb = $self->{dotsDb};

    my $naSeqId = $self->{naSeqId};
    my $start = $self->{start};
    my $end = $self->{end};
    my $tId = $self->{tableIds}->{'ExternalAASequence'};
    
    $self->{aaQuery} = 
        "select -1 as taxon_id, -1 as sequence_type_id, -1 as external_database_release_id, \n" .
        "eas.source_id, eas.description, \n" .
        "ss.query_start, ss.query_end, ss.subject_start, ss.subject_end, \n" .
        "ss.is_reversed, ss.score, ss.pvalue_mant, ss.pvalue_exp, \n" .
        "ss.number_identical, ss.match_length, s.number_identical as s_num_identical, \n" .
        "s.total_match_length as s_tot_match_length, s.score as s_score, \n" .
        "s.pvalue_mant as s_pavalue_mant, s.pvalue_exp as s_pavalue_exp, s.similarity_id \n" .
        "from ${dotsDb}.Similarity s, ${dotsDb}.ExternalAASequence eas, ${dotsDb}.SimilaritySpan ss \n" .
        "where s.query_table_id = 89 " .
        "and s.query_id = $naSeqId " .
        "and s.subject_table_id = $tId \n" .
        "and ss.query_start < $end " .
        "and ss.query_end > $start \n" .
        (defined($maxPValExp) ? "and ss.pvalue_exp <= $maxPValExp " : "").
        "and s.subject_id = eas.aa_sequence_id \n" .
        "and s.similarity_id = ss.similarity_id \n";
}

sub setNAQueryBase {
    my ($self, $seqIsSbj) = @_;

    my $dotsDb = $self->{dotsDb};

    my $naSeqId = $self->{naSeqId};
    my $start = $self->{start};
    my $end = $self->{end};
    my $tId = $self->{tableIds}->{'ExternalNASequence'};
 
    my $queryBaseKey = $seqIsSbj ? "naQueryBaseSubject" : "naQueryBaseQuery";

    if (!$self->{$queryBaseKey} ) {
        if ($seqIsSbj) {
            $self->{$queryBaseKey} =
                "select eas.taxon_id, eas.sequence_type_id, eas.external_database_release_id, \n" .
                "eas.source_id, eas.description, ss.subject_start, \n" .
                "ss.subject_end, ss.query_start, ss.query_end, ss.is_reversed, ss.score, \n" .
	        "ss.pvalue_mant, ss.pvalue_exp, ss.number_identical, ss.match_length, \n" .
                "s.number_identical as s_num_ident, s.total_match_length as s_tot_match_length, \n" .
                "s.score as s_score, s.pvalue_mant as s_pvalue_mant, s.pvalue_exp as s_pavalue_exp, \n" .
                "s.similarity_id \n" .
                "from ${dotsDb}.Similarity s, ${dotsDb}.ExternalNASequence eas, ${dotsDb}.SimilaritySpan ss \n" .
                "where s.subject_table_id = 89 " .
                "and s.subject_id = $naSeqId " .
                "and s.query_table_id = $tId \n" .
                "and ss.subject_start < $end " .
                "and ss.subject_end > $start \n" .
                "and s.query_id = eas.na_sequence_id \n";
        } else {
            $self->{$queryBaseKey} =
                "select eas.taxon_id, eas.sequence_type_id, eas.external_database_release_id, \n" .
                "eas.source_id, eas.description, ss.query_start, \n" .
                "ss.query_end, ss.subject_start, ss.subject_end, ss.is_reversed, ss.score, \n" .
	        "ss.pvalue_mant, ss.pvalue_exp, ss.number_identical, ss.match_length, \n" .
                "s.number_identical as s_num_ident, s.total_match_length as s_tot_match_length, \n" .
                "s.score as s_score, s.pvalue_mant as s_pvalue_mant, s.pvalue_exp as s_pavalue_exp, \n" .
                "s.similarity_id \n" .
                "from ${dotsDb}.Similarity s, ${dotsDb}.ExternalNASequence eas, ${dotsDb}.SimilaritySpan ss \n" .
                "where s.query_table_id = 89 " .
                "and s.query_id = $naSeqId " .
                "and s.subject_table_id = $tId \n" .
                "and ss.query_start < $end " .
                "and ss.query_end > $start \n" .
                "and s.subject_id = eas.na_sequence_id \n";
        }
        $self->{$queryBaseKey} .= "and s.similarity_id = ss.similarity_id \n";
    }
}

sub buildNAQuery {
    my ($self, $maxPValExp, $seqIsSubject, $estAndGss, $taxonIds, $extDbId) = @_;

    $self->setNAQueryBase($seqIsSubject);

    my $stStr;
    if ($estAndGss =~ /only/i) {
        $stStr = "eas.sequence_type_id in (8,21) ";
    } elsif ($estAndGss =~ /none/i) {
        $stStr = "eas.sequence_type_id not in (8,21) ";
    } 

    my $taxStr;
    if (defined($taxonIds)) {
        if (scalar(@$taxonIds) == 1) {
            my $tid = $taxonIds->[0];
	    $taxStr = "eas.taxon_id = $tid ";
        } else {
	    $taxStr = "eas.taxon_id in (" . join(',', @$taxonIds) . ")";
	}
    }

    my @andClauses;
    push(@andClauses, $taxStr) if $taxStr;
    push(@andClauses, $stStr) if $stStr;
    push(@andClauses, "ss.pvalue_exp <= $maxPValExp") if $maxPValExp;
    push(@andClauses, "eas.external_database_release_id = $extDbId") if $extDbId;

    my $orClause = "(" . join(' and ', @andClauses) . ")\n";

    my @ors;
    if ($seqIsSubject) {
      @ors = @{ $self->{naQueryOrsSubject} };
      push (@ors, $orClause);
      $self->{naQueryOrsSubject} = \@ors;
    } else {
      @ors = @{ $self->{naQueryOrsQuery} };
      push (@ors, $orClause);
      $self->{naQueryOrsQuery} = \@ors;
    }
}

sub buildTAQuery {
    my ($self, $maxPValExp) = @_;

    my $dotsDb = $self->{dotsDb};

    my $naSeqId = $self->{naSeqId};
    my $start = $self->{start};
    my $end = $self->{end};
    my $tId = $self->{tableIds}->{'TranslatedAASequence'};

    # HACK-
    # For GeneFeature this is implemented as a "reverse" query (e.g.
    # GeneFeatures were the query sequences in a search of this genomic
    # sequence.)  There are also some extra joins because the query is
    # assumed to be of the protein translations for the GeneFeatures.
    #
    $self->{taQuery} = "select -2 as taxon_id, -2 as sequence_type_id, -2 as external_database_release_id, \n" . 
                "gf.source_id, gf.product as description, \n" .
                "ss.subject_start, ss.subject_end, ss.query_start, ss.query_end, \n" .
                "ss.is_reversed, ss.score, ss.pvalue_mant, ss.pvalue_exp, \n" .
                "ss.number_identical, ss.match_length, s.similarity_id \n" .
                "from ${dotsDb}.Similarity s, ${dotsDb}.TranslatedAAFeature taf, \n" .
                "     ${dotsDb}.RNAFeature rnaf, ${dotsDb}.GeneFeature gf, ${dotsDb}.ProjectLink pl, \n" .
                "     ${dotsDb}.SimilaritySpan ss \n" .
                "where s.subject_table_id = 89 " .
                "and s.subject_id = $naSeqId " .
                "and s.query_table_id = $tId \n" .
                "and ss.subject_start < $end " .
                "and ss.subject_end > $start \n" .
                (defined($maxPValExp) ? "and ss.pvalue_exp <= $maxPValExp " : "").
                "and s.query_id = taf.aa_sequence_id \n" .
                "and taf.na_feature_id = rnaf.na_feature_id \n" .
                "and rnaf.parent_id = gf.na_feature_id \n" .
                "and gf.na_feature_id = pl.id " .
                "and pl.table_id = 108 " .
                "and pl.project_id = 113 \n" .                    # HACK
                "and s.similarity_id = ss.similarity_id\n";
}

sub getSimilaritySpanArray {
    my ($self) =  @_;

    my $rs = $self->getSimilaritySpanResultSet;
    return unless $rs;

    my $simRows = [];
    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSSimilarityQueryBuilder::getSimilaritySpanArray::looping through ResultSet",
                                 -1, "\t") if $WDK::Model::GDUtil::DEBUG::debug;
    while (my @row = $rs->fetchrow_array()) {
        push(@$simRows, \@row);
    }
    $rs->finish();
    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSSimilarityQueryBuilder::getSimilaritySpanArray::done",
                              $lastcall, "\t") if $WDK::Model::GDUtil::DEBUG::debug;
    return $simRows;
}

sub getSimilaritySpanResultSet {
    my ($self) =  @_;

    my $dbh = $self->{dbh};
    my $aa = $self->{aaQuery};
    my $ta = $self->{taQuery};

    my ($naSubject, $naQuery);
    my $nabS = $self->{naQueryBaseSubject};
    my $naosS = $self->{naQueryOrsSubject};
    if ($nabS && $naosS) {
        $naSubject = $nabS . " and (" . join(' or ', @$naosS) . ")\n";
    }
    my $nabQ = $self->{naQueryBaseQuery};
    my $naosQ = $self->{naQueryOrsQuery};
    if ($nabQ && $naosQ) {
        $naQuery = $nabQ . " and (" . join(' or ', @$naosQ) . ")\n";
    }

    my @subs;
    push (@subs, "\n${naSubject}") if $naSubject;
    push (@subs, "\n${naQuery}") if $naQuery;
    push (@subs, "\n${aa}") if $aa;
    push (@subs, "\n${ta}") if $ta;
    
    return unless @subs;

    my $sql = "select * from ("
            . join(' union ', @subs)
            . ") order by taxon_id, sequence_type_id, external_database_release_id, source_id, subject_start";

#    print STDERR "GUSSimilarityQueryBuilder.getSimilaritySpanResultSet: sql = \n$sql \n";

    my $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSSimilarityQueryBuilder::getSimilaritySpanResultSet::sql=\n$sql",
                                 -1, "\t") if $debug;
    my $rs = $dbh->prepare($sql);
    $rs->execute();
    $lastcall = &WDK::Model::GDUtil::Transducer::DEBUG::logTS("GUSSimilarityQueryBuilder::getSimilaritySpanResultSet::executed",
                              $lastcall, "\t") if $debug;
   
    # expect caller to close result set
    return $rs;
}

sub getTableIds {
    my($self, $tnames) = @_;

    my $coreDb = $self->{coreDb};
    my $dbh = $self->{dbh};
    my %tids;
    
    my $inClause = join(', ', map { s/(.*)/\'$1\'/; $_ } @$tnames);


    my $sql = "select name, table_id from ${coreDb}.TableInfo where name in ($inClause)";

#    print STDERR "GUSSimilarityQueryBuilder: sql = $sql \n";

    my $sth = $dbh->prepare($sql);
    $sth->execute();
    while (my @row = $sth->fetchrow_array()) {
        $tids{$row[0]} = $row[1];
    }
    $sth->finish();

    return \%tids;
}

1;
