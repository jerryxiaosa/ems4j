{{- define "ems-app.backendFullImage" -}}
{{- printf "%s/%s:%s" .Values.image.registry .Values.backend.image.repository .Values.backend.image.tag -}}
{{- end -}}

{{- define "ems-app.frontendFullImage" -}}
{{- printf "%s/%s:%s" .Values.image.registry .Values.frontend.image.repository .Values.frontend.image.tag -}}
{{- end -}}

{{- define "ems-app.iotFullImage" -}}
{{- printf "%s/%s:%s" .Values.image.registry .Values.iot.image.repository .Values.iot.image.tag -}}
{{- end -}}

{{- define "ems-app.iotSimulatorFullImage" -}}
{{- printf "%s/%s:%s" .Values.image.registry .Values.iotSimulator.image.repository .Values.iotSimulator.image.tag -}}
{{- end -}}

{{- define "ems-app.timezoneEnv" -}}
- name: TZ
  value: {{ .Values.global.timezone | quote }}
{{- end -}}

{{- define "ems-app.javaTimezoneEnv" -}}
- name: JAVA_TOOL_OPTIONS
  value: {{ .Values.global.javaToolOptions | quote }}
{{- end -}}

{{- define "ems-app.timezoneVolumeMounts" -}}
- name: timezone-config
  mountPath: /etc/localtime
  readOnly: true
{{- end -}}

{{- define "ems-app.timezoneVolumes" -}}
- name: timezone-config
  hostPath:
    path: {{ printf "/usr/share/zoneinfo/%s" .Values.global.timezone | quote }}
    type: File
{{- end -}}
