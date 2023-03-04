Actualment estàs subscrit i reps notificacions 15 minuts, 1 hora i 3 hores abans de:

<#list subscriptions as subscription>
<b><u>${subscription.seriesName}</u></b>

- Curses
<#if subscription.notifyQualifying = true>- Sessions de classificació</#if>
<#if subscription.notifyPractice = true>- Entrenaments lliures</#if>
</#list>
