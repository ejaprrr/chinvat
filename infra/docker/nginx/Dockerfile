FROM nginx:1.27-alpine

RUN apk add --no-cache gettext openssl

COPY infra/docker/nginx/templates/gateway.conf.template /etc/nginx/templates/gateway.conf.template
COPY infra/docker/nginx/entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]

