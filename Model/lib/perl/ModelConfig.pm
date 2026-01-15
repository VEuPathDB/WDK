package WDK::Model::ModelConfig;

use strict;
use XML::Simple;
use WDK::Model::DbUtils qw(jdbc2oracleDbi dbi2connectString);

sub new {
    my ($class, $model) = @_;

    my $self = {};
    bless $self;

    my $cfg;

    if ( ref($model) eq 'HASH' ) {
        $cfg = $model;
    }
    else {
        my $modelconfig = "$ENV{GUS_HOME}/config/${model}/model-config.xml";
        (-e $modelconfig) or die "File not found: $modelconfig\n";
        $cfg = XMLin($modelconfig);
    }

    for (keys %$cfg) {
        $self->{$_} = $cfg->{$_};
        if (ref($cfg->{$_}) eq 'HASH') {
            if (defined $cfg->{$_}->{connectionUrl} && $cfg->{$_}->{connectionUrl} ne '') {
                # add entry for Perl DBI DSN. e.g. dbi:Oracle:toxo440s
                $self->{$_}->{dbiDsn} = jdbc2postgresDbi($cfg->{$_}->{connectionUrl});
                # add entry for connection string. e.g. toxo440s
                ($self->{$_}->{connectString} = $self->{$_}->{dbiDsn}) =~ s/dbi:Pg://;
            }
            elsif (defined $cfg->{$_}->{ldapCommonName} && $cfg->{$_}->{ldapCommonName} ne '') {
		my $cn = $cfg->{$_}->{ldapCommonName};
		my $host;
		if ($cn eq "genomicsdb_068n" || $cn eq "genomicsdb_devn"){
		    $host = "ares13.penn.apidb.org";
		} elsif ($cn eq "genomicsdb_rebuild01"){
		    $host = "ares9.penn.apidb.org";
		} else {
		    $host = "ares12.penn.apidb.org";
		}
		# add entry for Perl DBI DSN. e.g. dbi:Oracle:toxo440s
		$self->{$_}->{dbiDsn} = 'dbi:Pg:dbname='.$cn.';host='.$host;
		# add entry for connection string. e.g. toxo440s
		$self->{$_}->{connectString} = $cn;
            }
        }
    }

    return $self;
}

# This is a "method proxy" for getters.  It can be called like this:
#   $self->getLogin, $self->getLogin(), $self->login or $self->login()
# where <login> is a root element in the model-config file.
#
# The method is recursive so it can also handle $self->getQueryMonitor->getBaseline
# where <queryMonitor> is an element and baseline= is an attribute of that element
#
# It dies if the accessed element or attribute is not found.
# To test for existence either catch the exception or use $self->{login}
sub AUTOLOAD {
    my $attr = our $AUTOLOAD;
    $attr =~ s/.*:://;
    return if $attr =~ /^[A-Z]+$/;  # skip methods such as DESTROY
    $attr =~ s/get([A-Z])/$1/;
    $attr = lcfirst($attr);
    my $retVal = $_[0]->{ $attr } || die "`$attr' not defined.";
    return (ref ($retVal) eq "HASH")
        ? new WDK::Model::ModelConfig($retVal)
        : $retVal;
}


# return to appDb value, for backward compatibility
sub getAppDbLogin {
    my ($self) = @_;
    ($self->{appDb})
        ? $self->{appDb}->{login}
        : $self->{login}
}

# return to appDb value, for backward compatibility
sub getAppDbPassword {
    my ($self) = @_;
    ($self->{appDb})
        ? $self->{appDb}->{password}
        : $self->{password}
}

# return to appDb value, for backward compatibility
sub getAppDbDbiDsn {
    my ($self) = @_;
    ($self->{appDb})
        ? $self->{appDb}->{dbiDsn}
        : $self->{dbiDsn}
}

# return to userDbLink value, for backward compatibility
sub getDblApicomm {
    my ($self) = @_;
    ($self->{appDb})
        ? $self->{appDb}->{userDbLink}
		: $self->{userDbLink}
}

# return to appDb value, for backward compatibility
sub getUserDbLogin {
  my ($self) = @_;
  return $self->{userDb}->{login};
}

sub getUserDbPassword {
  my ($self) = @_;
  return $self->{userDb}->{password};

}

# return to appDb value, for backward compatibility
sub getUserDbDbiDsn {
    my ($self) = @_;
    return $self->{userDb}->{dbiDsn};
}



1;



__END__

=head1 NAME

WDK::Model::ModelConfig - access to WDK model-config.xml properties

=head1 SYNOPSIS

    use WDK::Model::ModelConfig;

    my $cfg = new WDK::Model::ModelConfig('TrichDB');
    
    my $username = $cfg->getAppDb->getLogin;
    my $password = $cfg->getAppDb->getPassword;
    my $accountDb_DBI = $cfg->getAccountDb->getDbiDsn;
    my $emailSubject = $cfg->getEmailSubject;
    
    Retrieve the JDBC connectionUrl converted to Perl DBI syntax:
    my $dsn = $cfg->getAppDb->getDbiDsn;

    You may also access by property name:
        $cfg->appDb->login
        $cfg->userDb->login
        $cfg->emailSubject
        
    $cfg->appDb->connectionUrl is the JDBC connection string as written in the 
    model-config.xml.
    $cfg->apiDb->dbiDsn is the Perl DBI version translated from the 
    connectionUrl property.
    
=head1 DESCRIPTION

Provides Perl access to properties in a WDK model-config.xml file.

=head1 BUGS

The conversion of the JDBC connectionUrl to Perl DBI only works for Oracle
thin driver syntax, and even then not for all allowed syntax. See 
WDK::Model::DbUtils for supported syntax.

=head1 AUTHOR 

Mark Heiges, mheiges@uga.edu

=cut

=head1 METHODS

=head2 new

 Usage   : my $cfg = new WDK::Model::ModelConfig('TrichDB');
 Returns : object containing data parsed from the model configuration file.
 Args    : the name of the model. This follows the name convention used for
           the WDK commandline utilities. This is used to find the Model's 
           configuration XML file ($GUS_HOME/config/{model}/model-config.xml)

=head2 getLogin
 
 Usage : my $username = $cfg->getAppDb->getLogin;
 Returns : login name for the database
 
=head2 getPassword
 
 Usage : my $passwd = $cfg->getAppDb->getPassword;
 Returns : login password for the database
 
=head2 getDbiDsn
 
 Usage : my $dsn = $cfg->getAppDb->getDbiDsn;
 Returns : perl dbi connection string. converted from the jdbc connection URL in the model-config.xml
 Example : dbi:Oracle:host=redux.rcc.uga.edu;sid=trichsite
 
=head2 getConnectionUrl
 
 Usage : my $jdbcUrl = $cfg->getAppDb->getConnectionUrl;
 Returns : original jdbc connection string from model-config.xml

=head2 getConnectString
 
 Usage : my $connect = $cfg->getAppDb->getConnectString;
 Returns : connect string suitable for non-DBI cases (e.g. sqlplus)



=head2 getQueryInstanceTable

=head2 getQueryHistoryTable 

=head2 getPlatformClass     

=head2 getMaxQueryParams    

=head2 getMaxIdle           

=head2 getMaxWait           

=head2 getMaxActive         

=head2 getMinIdle           

=head2 getInitialSize       

=head2 getWebServiceUrl     

 
=cut

