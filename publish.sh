echo "$pgp_secret_key" > private.key
echo $pgp_passphrase | gpg --passphrase-fd 0 --batch --yes --import private.key
rm private.key

export GPG_TTY=$(tty)
pwd
ls
gpg --clearsign build.sc
#./mill -i mill.scalalib.PublishModule/publishAll --sonatypeCreds $sonatype_credentials --gpgPassphrase $pgp_passphrase --publishArtifacts __.publishArtifacts --release false
