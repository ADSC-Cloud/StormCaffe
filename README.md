# StormCaffe
A storm-based deep learning framework specified for video analytic workloads.

This work is finished by He Jiong (john.hejiong@gmail.com) at Advanced Digital Sciences Center (ADSC). Currently, StormCaffe integrates Caffe (http://caffe.berkeleyvision.org), a commonly used deep learning framework, into Apache Storm (http://storm.apache.org) so that video analytics can be easily deployed on clusters with a large number of nodes to provide high scalability and real-time processing capability. However, it can ben easily extended to other deep learning frameworks such as Mxnet and Tensorflow thanks to the JavaCPP Presets (https://github.com/bytedeco/javacpp-presets). Besides, it can be extended to any C/C++ based deep learning frameworks based on Java-CPP interface, JavaCPP (https://github.com/bytedeco/javacpp).

StormCaffe provides generic Spouts and Bolts for various situations in video analytics (such as optical flow, RGB computing and so on) so that users only need to inherit these components and define their own topologies, and leave everything else to Storm to fully utilize the computing capability of clusters.
