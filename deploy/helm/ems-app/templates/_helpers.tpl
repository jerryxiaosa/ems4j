{{- define "ems-app.defaultImageTag" -}}
{{- default .Chart.AppVersion .Values.global.imageTag -}}
{{- end -}}

{{- define "ems-app.backendFullImage" -}}
{{- $tag := default (include "ems-app.defaultImageTag" .) .Values.backend.image.tag -}}
{{- printf "%s/%s:%s" .Values.image.registry .Values.backend.image.repository $tag -}}
{{- end -}}

{{- define "ems-app.frontendFullImage" -}}
{{- $tag := default (include "ems-app.defaultImageTag" .) .Values.frontend.image.tag -}}
{{- printf "%s/%s:%s" .Values.image.registry .Values.frontend.image.repository $tag -}}
{{- end -}}

{{- define "ems-app.iotFullImage" -}}
{{- $tag := default (include "ems-app.defaultImageTag" .) .Values.iot.image.tag -}}
{{- printf "%s/%s:%s" .Values.image.registry .Values.iot.image.repository $tag -}}
{{- end -}}

{{- define "ems-app.iotSimulatorFullImage" -}}
{{- $tag := default (include "ems-app.defaultImageTag" .) .Values.iotSimulator.image.tag -}}
{{- printf "%s/%s:%s" .Values.image.registry .Values.iotSimulator.image.repository $tag -}}
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
