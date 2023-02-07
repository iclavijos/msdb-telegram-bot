Actualment estàs subscrit i reps notificacions 15 minuts, 1 hora i 3 hores abans de:

<#list subscriptions as subscription>
<b><u>${subscription.seriesName}</u></b>

- Races
<#if subscription.notifyQualifying = true>- Qualifying sessions</#if>
<#if subscription.notifyPractice = true>- Practice sessions</#if>
</#list>