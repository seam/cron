# Service Provider Interface (SPI)

The SPI is used by those wishing to implement a Cron feature using a specific technology stack (eg: implementing scheduling using the 
Quartz scheduler).

## Architecture



### Testing

The cron-tck subproject contains reusable tests which any provider can plug into in order to run those tests against their provider.
This is the best way of determining whether a provider properly implements the intended feature set.
To have your the TCK run against your provider implementation, you simply:

 # Extend the ...

## Feature Specific

### Scheduling

During startup, Cron will inspect all CDI beans looking for observer methods which use the @Scheduled or @Every qualifiers. 
For each such observer method Cron will generate a TrigerDetail (TODO -> replace TriggerSupplies with this, create NonCDIObserverDetail to replace existing TriggerSupplies instances, then rename that to ObserverDetail, and rename fetchTriggerSupplies to fetchObserverDetail) instance which captures the configuration of that schedule or increment.
It will then pass those configurations to the scheduling provider implementation to handle in its own way.
The implementation will use its own internal API to schedule code to execute according to the specified schedule.
That code must result in the firing of a CDI event corresponding to the observer method from which the configuration was generated.
The net result is that each scheduled observer method is executed according to the schedule it defines.

#### ObserverDetail (was TrigerDetail)

A scheduling provider must know the appropriate qualifier(s) to use when firing the CDI event method so that the corresponding scheduled observer method is invoked.
This data is encapsulated in the ObserverDetail class.
Providers must use these details to configure and fire an event having the appropriate qualifiers to trigger the observer method from which the scheduling configuration was originally retrieved.
TriggerSupport (see below) is a useful helper class for creating and firing such an event.

#### TriggerSupport

This utility class simplifies the firing of the appropriate event for a specific scheduled observer method. 
When a scheduling provider uses their own internal API to create a scheduled code execution, that code which is executed should typically be a call to TriggerSupport.fireTrigger(), having first configured the TriggerSupport with the appropriate ObserverDetail instance.
The fireTrigger() method will create an instance of Trigger containing the current time (and current second/minute/hour in the case of @Every), apply the qualifiers found in ObserverDetail to the event and fire it using the standard CDI Event interface.



