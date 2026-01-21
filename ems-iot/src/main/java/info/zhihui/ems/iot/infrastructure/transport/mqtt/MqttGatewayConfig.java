//package info.zhihui.ems.iot.infrastructure.transport.mqtt;
//
//import info.zhihui.ems.iot.config.DeviceAdapterProperties;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//import org.springframework.integration.annotation.ServiceActivator;
//import org.springframework.integration.channel.DirectChannel;
//import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
//import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
//import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
//import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
//import org.springframework.integration.mqtt.support.MqttHeaders;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.MessageHandler;
//
//@Configuration
//@RequiredArgsConstructor
//@Slf4j
//@Profile("mqtt")
//public class MqttGatewayConfig {
//
//    private final DeviceAdapterProperties properties;
//
//    @Bean
//    public MqttPahoClientFactory mqttClientFactory() {
//        DeviceAdapterProperties.MqttProperties props = properties.getMqtt();
//        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
//        MqttConnectOptions connectOptions = new MqttConnectOptions();
//        if (props.getUrl() != null) {
//            connectOptions.setServerURIs(new String[]{props.getUrl()});
//        }
//        if (props.getUsername() != null) {
//            connectOptions.setUserName(props.getUsername());
//        }
//        if (props.getPassword() != null) {
//            connectOptions.setPassword(props.getPassword().toCharArray());
//        }
//        factory.setConnectionOptions(connectOptions);
//        return factory;
//    }
//
//
//    @Bean
//    public MessageChannel mqttInputChannel() {
//        return new DirectChannel();
//    }
//
//    @Bean
//    public MqttPahoMessageDrivenChannelAdapter inbound() {
//        DeviceAdapterProperties.MqttProperties props = properties.getMqtt();
//        MqttPahoMessageDrivenChannelAdapter adapter =
//                new MqttPahoMessageDrivenChannelAdapter(props.getClientId(), mqttClientFactory(), props.getTopics());
//        adapter.setCompletionTimeout(5000);
//        adapter.setConverter(new DefaultPahoMessageConverter());
//        adapter.setQos(1);
//        adapter.setOutputChannel(mqttInputChannel());
//        return adapter;
//    }
//
//    @Bean
//    @ServiceActivator(inputChannel = "mqttInputChannel")
//    public MessageHandler mqttMessageHandler() {
//        return message -> {
//            String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
//            String payload = (String) message.getPayload();
//            log.info("MQTT packet topic={} payload={}", topic, payload);
//
//        };
//    }
//}
