{{- define "ems-infra.mysqlFullImage" -}}
{{- printf "%s:%s" .Values.mysql.image.repository .Values.mysql.image.tag -}}
{{- end -}}

{{- define "ems-infra.redisFullImage" -}}
{{- printf "%s:%s" .Values.redis.image.repository .Values.redis.image.tag -}}
{{- end -}}

{{- define "ems-infra.rabbitmqFullImage" -}}
{{- printf "%s/%s:%s" .Values.image.registry .Values.rabbitmq.image.repository .Values.rabbitmq.image.tag -}}
{{- end -}}

{{- define "ems-infra.timezoneEnv" -}}
- name: TZ
  value: {{ .Values.global.timezone | quote }}
{{- end -}}

{{- define "ems-infra.timezoneVolumeMounts" -}}
- name: timezone-config
  mountPath: /etc/localtime
  readOnly: true
{{- end -}}

{{- define "ems-infra.timezoneVolumes" -}}
- name: timezone-config
  hostPath:
    path: {{ printf "/usr/share/zoneinfo/%s" .Values.global.timezone | quote }}
    type: File
{{- end -}}
