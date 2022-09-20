# jtunnel


Test
=======
## Running server

The server is also a java application . It does not support https for incoming requests . To Run this

1. Terminate TLS using some LB(Load Balancer) like nginx/caddy etcc.
2. Configure this server as a backend to the LB.
3. Valid certificates are needed for working with webhooks .

