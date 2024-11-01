# Databus Metadata Overlay Search System LODGEOSS

This is an old version of MOSS a result of the LODGEOSS project. You can find the new version of MOSS [here](https://github.com/dbpedia/databus-moss).

demo at [moss.tools.dbpedia.org](https://moss.tools.dbpedia.org/)

## Setup of the services

There are multiple services involved to make the MOSS system work:

### Ontology Lookup

This service is used to find the ontology terms to look for in the (meta)data.
The lookup is running on the tools server and is loaded with some ontologies (currently only a few). It can be tested at [http://tools.dbpedia.org:9274/lookup-application/api/search](http://tools.dbpedia.org:9274/lookup-application/api/search) (still need to set the `?query=xyz` param)


### Mods Endpoint

The mods endpoint saves the annotated metadata. Currently, it is available at [https://mods.tools.dbpedia.org/sparql](https://mods.tools.dbpedia.org/sparql), but there `nginx` works as a proxy pass to the actual server, which is located at [http://akswnc7.informatik.uni-leipzig.de:28890/sparql](http://akswnc7.informatik.uni-leipzig.de:28890/sparql).  


### Continuous Deploy

Currently setup on the `tools` server, **TODO** Marvin.
