FROM rabbitmq:4.1-management

ARG RABBITMQ_DELAYED_PLUGIN_URL=https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v4.1.0/rabbitmq_delayed_message_exchange-4.1.0.ez

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && curl -fL "$RABBITMQ_DELAYED_PLUGIN_URL" -o /plugins/rabbitmq_delayed_message_exchange-4.1.0.ez \
    && rabbitmq-plugins enable --offline rabbitmq_delayed_message_exchange
