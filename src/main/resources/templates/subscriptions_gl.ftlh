Actualmente estás subscrito e recibes notificacións 15 minutos, 1 hora e/ou 3 horas antes, segundo as túas preferencias, de:

<#list subscriptions as subscription>
<b><u>${subscription.seriesName}</u></b>

- Carreiras
<#if subscription.notifyQualifying = true>- Sesións de clasificación</#if>
<#if subscription.notifyPractice = true>- Entrenamentos libres</#if>
</#list>
