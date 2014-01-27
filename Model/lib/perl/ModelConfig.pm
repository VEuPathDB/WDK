package WDK::Model::ModelConfig;

use strict;
use XML::Simple;


=head1 NAME

WDK::Model::ModelConfig - access to WDK model-config.xml attributes

=head1 SYNOPSIS

    use WDK::Model::ModelConfig;
    my $cfg = WDK::Model::ModelConfig->new('TemplateDB');

Attribute values are accessed by name.

    my $supportEmail = $cfg->supportEmail;
    my $emailSubject = $cfg->emailSubject;

Include the element name when accessing nested values.

    my $username = $cfg->appDb->login;
    my $password = $cfg->userDb->password;

undef is returned for not found in the model-config.xml

    print "foo not found\n" if not defined $c->foo;

=head1 DESCRIPTION

Provides Perl access to attributes in a WDK model-config.xml file.

=head1 METHODS

=head2 new

 Usage   : my $cfg = new WDK::Model::ModelConfig('TemplateDB');
 Returns : object containing data parsed from the model configuration file.
 Args    : the name of the model. This follows the name convention used for
           the WDK commandline utilities. This is used to find the model's 
           configuration XML file:
           $GUS_HOME/config/{model}/model-config.xml
=cut
sub new {
    my ($class, $model) = @_;

    my $self = {};
    bless $self;

    my $cfg;
    
    if ( ref($model) eq 'HASH' ) {
        $cfg = $model;
    } else {
        my $modelconfig = "$ENV{GUS_HOME}/config/${model}/model-config.xml";
        (-e $modelconfig) or die "File not found: $modelconfig\n";
        $cfg = XMLin($modelconfig);
    }
    
    for (keys %$cfg) {
        $self->{$_} = $cfg->{$_};
        if (ref($cfg->{$_}) eq 'HASH' && defined $cfg->{$_}->{connectionUrl}) {
            # add entry for Perl DBI DSN. e.g. dbi:Oracle:toxo440s
            $self->{$_}->{dbiDsn} = _jdbc2oracleDbi($self->{$_}->{connectionUrl});
            # add entry for connection string. e.g. toxo440s
            ($self->{$_}->{connectString} = $self->{$_}->{dbiDsn}) =~ s/dbi:Oracle://;    
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
    my $retVal = $_[0]->{ $attr } || return undef;
    return (ref ($retVal) eq "HASH")
        ? new WDK::Model::ModelConfig($retVal)
        : $retVal;
}

sub _jdbc2oracleDbi {
    my ($jdbc) = @_;
    if ($jdbc =~ m/thin:[^@]*@([^:]+):([^:]+):([^:]+)/) {
        # jdbc:oracle:thin:@redux.rcc.uga.edu:1521:cryptoB
        my ($host, $port, $sid) = $jdbc =~ m/thin:[^@]*@([^:]+):([^:]+):([^:]+)/;
        return "dbi:Oracle:host=$host;sid=$sid;port=$port";
    } elsif ($jdbc =~ m/@\(DESCRIPTION/i) {    
        # jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=redux.rcc.uga.edu)(PORT=1521))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=cryptoB.rcc.uga.edu)))
        my ($tns) = $jdbc =~ m/[^@]+@(.+)/;
        return "dbi:Oracle:$tns";
    } elsif ($jdbc =~ m/:oci:@/) {
       # jdbc:oracle:oci:@toxoprod
       my ($sid) = $jdbc =~ m/:oci:@(.+)/;
        return "dbi:Oracle:$sid";
    } else {
        # last ditch effort.
        # jdbc:oracle:thin:@kiwi.rcr.uga.edu/cryptoB.kiwi.rcr.uga.edu
        $jdbc =~ m/thin:[^@]*@(.+)/;
        return "dbi:Oracle:$1";
    }
}

1;

=head1 AUTHOR 

EuPathDB, help@eupathdb.org

=cut
