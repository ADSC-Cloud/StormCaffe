# StormCaffe
A storm-based deep learning framework specified for video analytic workloads.

This work is finished by He Jiong (john.hejiong@gmail.com) at Advanced Digital Sciences Center (ADSC). StormCaffe integrates Caffe (http://caffe.berkeleyvision.org), a commonly used deep learning framework, into Apache Storm (http://storm.apache.org) so that video analytics can be easily deployed on clusters with a large number of nodes to provide high scalability and real-time processing capability.

StormCaffe provides generic Spouts and Bolts for various situations in video analytics (such as optical flow, RGB computing and so on) so that users only need to inherit these components and define their own topologies, and leave everything else to Storm to fully utilize the computing capablity of clusters.