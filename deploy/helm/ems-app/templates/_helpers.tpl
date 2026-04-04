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
