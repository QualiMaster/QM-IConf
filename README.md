# QM-IConf
The QualiMaster (http://qualimaster.eu) infrastructure configuration tool (QM-IConf) allows configuring the QualiMaster infrastructure (https://github.com/QualiMaster/Infrastructure) as 
well as the data analysis pipelines running on a certain infrastructure. In particular, it allows configuring data analysis pipelines in a graphical drag-and-drop fashion and enables
the user to generate the respective pipeline code from the configuration. QM-IConf is a domain-specific graphical frontend for the Open Source product 
line toolset EASy-Producer (https://github.com/SSEHUB/EASyProducer).

For developing QM-IConf, the following parts must be installed:
  - EASy-Producer (https://github.com/SSEHUB/EASyProducer) as source code or as release bundles.
  - EASy-Producer extensions for the QualiMaster (https://github.com/QualiMaster/QM-EASyProducer)
  - Eugenia diagram editor generation framework
  - Emfatic EMF editor
  - Graphical Modeling Framework
  - Graphical Modeling Framework tools (experimental SDK)
  
For more (technical) details, please refer to the readme file.

QM-IConf is released as open source under the Apache 2.0 license.

Build Status
------------------
| Component | Status |
|---|---|
| ConfigurationApplication | ![Build Status](http://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=ConfigurationApplication) |
| ConfigurationApplication Nightly Update Site | ![Build Status](http://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=ConfigurationApplication.Nightly) |
| QualiMaster-Extension for EASy-Producer | ![Build Status](http://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=QualiMaster.Extension) |
| EASy-Producer | ![Build Status of EASy-Producer](http://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=EASy-Producer) |
| Instantiation | ![Build Status of Instantiation](http://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=Instantiation) |
| SSE-Reasoner | ![Build Status of SSE-Reasoner](http://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=SSEreasoner) |
| Reasoner Core | ![Build Status of Reasoner Core](http://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=ReasonerCore) |
| IVML-Parser | ![Build Status of IVML-Parser](http://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=IVML) |
| Variability Model | ![Build Status of Variability Model](http://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=VarModel) |
