Actualmente estás suscrito y recibes notificaciones 15 minutos, 1 hora y 3 horas antes de:

<#list subscriptions as subscription>
<b><u>${subscription.seriesName}</u></b>

- Carreras
<#if subscription.notifyQualifying = true>- Sesiones de clasificación</#if>
<#if subscription.notifyPractice = true>- Entrenamientos libres</#if>
</#list>