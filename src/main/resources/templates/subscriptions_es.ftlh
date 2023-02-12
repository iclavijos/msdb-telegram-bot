Actualmente estás suscrito y recibes notificaciones 15 minutos, 1 hora y 3 horas antes de:

<#list subscriptions as subscription>
<b><u>${subscription.seriesName}</u></b>

- Races
<#if subscription.notifyQualifying = true>- Qualifying sessions</#if>
<#if subscription.notifyPractice = true>- Practice sessions</#if>
</#list>