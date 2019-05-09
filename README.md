# eva-catalog

The eva-catalog service exists to provide a central repository (and client)
for handling the configuration maps used to connect to Eva.

## Definitions

For now, the catalog service holds and distributes fully contained flat
configuration maps. These maps are keyed by three values: the *tenant*,
the *category*, and the *label*.

The *tenant* for a configuration is a partitioning of ownership for
configurations.

The *category* for a configuration indicates a cross-configuration
coupling. Databases that will require cross-database queries should
share a common category.

The *label* on a configuration is a name that, along with the *tenant*
and *category* uniquely identifies a configuration for a specific
database.
