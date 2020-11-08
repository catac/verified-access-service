#!/bin/bash

# This script will sign a SPKAC request received in stdin and produce
# a certificate in format DER, encoded in base64, in stdout.

# The SPKAC request in input has this format (strip leading # )
# SPKAC=MIICgDCCAWgwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDOVIlZUecojefmstYFT4e7Li1jgDONpQb4YGgR//VQaauYQv5/r4A4jbi8gh9K5H8MCxK7GRGTvv2frr3b7qHNl+WSo6sSA/bUAUR+uYDKJfwEShlvnN225xDp+aokj6LwGP235qj1aeFBOQaTL7G0iQszdSTmg/BiOrh0s4yMYHp6b+Qh8ii5fpcH+1Qjt1C6MUk1FDkefzaeIxtkJgWw4dCfb9wSWMrAEugJbEz7PpG18DajNQkNknVRvT8aqWeWUNwQ2DGpfV+hg092gTJrQ3A5p6t6TUGBaLh9RwGNx2/e0zJKPRtBVjA646dMObJgVbI9ZAUhNcOiS6nv8O+rAgMBAAEWQDFDNUQyQ0Q3OUQzMzExOENBQzA5RDBENUU1NTkzRjRGNzg1QjY4NDA5NzczNEVENkM5QjFFOTkzNkEyNzAzRTcwDQYJKoZIhvcNAQELBQADggEBADf9avjizSHcqzpyZq41ZYMR75CSJNEUEBEccFQUENUCklbVg2JXad5TiK1Qdg3+9rUtIQ+ZTGtdVJ09OzPLSBPrtm4+G3RwJhNwLF8i2bb7QYjGa+oxM0XKPxndsIJ+itq1atDwNeii9Kxdc+xCEs6ENWqOsJb3YJwDK1zAOUPvXubNUUAtreqXQiACoUdh9qFR8e2q6AkUhBk44UvPZIgX75M113e8BcbWxIcWFbulNQvqkr2Bq6dTF7zoTUO0tste5KKdmFIJiJWwKl2UYlXgMwKichrc8r79il1unQoZwgnfprxGLjKtyIrWhZVAtLHXwcXiOz4FX24cpZk9uHQ=
# CN=user@example.com
# C=Country
# ST=State
# L=City
# O=Organisation

cd $(dirname $0)
openssl ca -config ca.cnf -spkac /dev/stdin -out - | base64 -w 0
