{{- define "bankflow-service.selectorLabels" -}}
app.kubernetes.io/name: {{ .Release.Name }}
{{- end }}

{{- define "bankflow-service.labels" -}}
{{ include "bankflow-service.selectorLabels" . }}
app.kubernetes.io/part-of: bankflow
app.kubernetes.io/version: {{ .Values.image.tag | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}