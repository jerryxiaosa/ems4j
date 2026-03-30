{{- define "ems-app.backendFullImage" -}}
{{- printf "%s/%s:%s" .Values.image.registry .Values.backend.image.repository .Values.backend.image.tag -}}
{{- end -}}

{{- define "ems-app.frontendFullImage" -}}
{{- printf "%s/%s:%s" .Values.image.registry .Values.frontend.image.repository .Values.frontend.image.tag -}}
{{- end -}}
