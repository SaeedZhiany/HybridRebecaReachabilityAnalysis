hybrid reachability
{
  state var <#list vars as var>${var.name}<#if var_has_next>,</#if></#list>

  setting
  {
    <#if settingBlock.fixedSteps??>
    fixed steps ${settingBlock.fixedSteps}
    </#if>
    <#if settingBlock.time??>
    time ${settingBlock.time}
    </#if>
    <#if settingBlock.remainderEstimation??>
    remainder estimation 1e-${settingBlock.remainderEstimation}
    </#if>
    identity precondition
    <#if settingBlock.gnuplotOctagonVars??>
    gnuplot octagon <#list settingBlock.gnuplotOctagonVars as var>${var}<#if var_has_next>,</#if></#list>
    </#if>
    <#if settingBlock.adaptiveOrders??>
    adaptive orders { min ${settingBlock.adaptiveOrders[0]} , max ${settingBlock.adaptiveOrders[1]} }
    </#if>
    <#if settingBlock.cutoff??>
    cutoff 1e-${settingBlock.cutoff}
    </#if>
    <#if settingBlock.precision??>
    precision ${settingBlock.precision}
    </#if>
    <#if outputFileName??>
    output ${outputFileName}
    </#if>
    <#if settingBlock.maxJumps??>
    max jumps ${settingBlock.maxJumps}
    </#if>
    print on
  }

  modes
  {
    curMode
    {
      lti ode
      {
        <#if modeDefinition.ODEAssignments??>
        <#list modeDefinition.ODEAssignments as odeAsg>${odeAsg.left}' = ${odeAsg.right}<#if odeAsg_has_next>${'\n\t\t'}</#if></#list>
        </#if>
      }
      <#if modeDefinition.invariantCondition??>
      inv
      {
        ${modeDefinition.invariantCondition}
      }
      </#if>
    }
    terminateMode
    {
    }
  }

  jumps
  {
    curMode -> terminateMode
    <#if jumpDefinition.guardCondition??>
    guard { ${jumpDefinition.guardCondition} }
    </#if>
    <#if jumpDefinition.resetAssignments??>
    reset { <#list jumpDefinition.resetAssignments as reset>${reset.left}' := ${reset.right}</#list> }
    </#if>
    parallelotope aggregation {}
  }

  init
  {
    curMode
    {
      <#list vars as var>
      ${var.name} in [${var.lowerBound},${var.upperBound}]
      </#list>
    }
  }
}
