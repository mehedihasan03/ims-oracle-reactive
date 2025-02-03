# mra-ims-loan-portfolio

docker service create --name ims-07-loan-api --replicas 1  \
--mount type=volume,source=ims_log_data,destination=/app/log \
--network app_net ims-07-loan-api:20240227_194514
