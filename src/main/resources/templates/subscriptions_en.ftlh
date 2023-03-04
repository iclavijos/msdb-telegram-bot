You are currently subscribed, and receive notifications 15 minutes, 1 hours and 3 hours before each session, to:

<#list subscriptions as subscription>
<b><u>${subscription.seriesName}</u></b>

- Races
<#if subscription.notifyQualifying = true>- Qualifying sessions</#if>
<#if subscription.notifyPractice = true>- Practice sessions</#if>
</#list>
