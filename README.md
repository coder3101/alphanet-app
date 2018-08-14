# Alphanet : Neural Network Marketplace for Android

Alphanet is a neural network market place for Android. It uses Tensorflow Mobile and performs inference on device inference without any internet connectivity. Just download the model of you choice feed in the values and run.

[Get it on Google Play](https://play.google.com/store/apps/details?id=com.softminds.aislate)



### Concept of Runner UI

Runner UI are the User Interface that interacts with the end user to get values. They are the UI that are responsible for taking inputs from User in a beautiful way. These UI's are a separte activity and must inherit from RunnerUI Base.

Currently we support the following Runner UI's :

1. **Raw Input** : As the name suggests this UI is used when you are expecting a user to input Raw values currently only float32 values are allowed.
2. **SlateView** : This UI is generally used with Convolutional neural network and draws a slate on `n x m` pixel wide on the screen. The User can draw on the slate and slate records the values in greyscale. **THE VALUES THE SLATE UI RETURNS IS NORMALIZED WITH 255**
3. **Image Picker View** : To Come soon..

### Currently Supported Models 

|    Model Name     | Runner UI  | Network Type  | Test Accuracy |
| :---------------: | :--------: | :-----------: | :-----------: |
|  Iris Classifer   | Raw Input  | Feed Forward  |     99.0      |
| MNIST Classifier  | Slate View | Convolutional |     99.26     |
| EMNIST Classifier | Slate View | Convolutional |     87.26     |

Many other models will be added soon. Meanwhile if you want to run your own models. Follow the steps below.

### Running your own model

The Application comes with the feature of running any Frozen tensorflow graph that are optimized for inference for Android. Here is a simple way to convert your tensorflow code to optimized profiles.

```python
def save_and_generate_proto(session, name=None):
  from tensorflow.python.framework import graph_util
  from tensorflow.python.framework import graph_io
  from tensorflow.python.tools import optimize_for_inference_lib as opt
  from tensorflow.python.tools import freeze_graph as fg

  target_path="./"
  name = name or "model-name"
  tf.train.write_graph(session.graph_def, target_path, name+'.pbtxt')
  tf.train.Saver().save(session, os.path.join(target_path, name+'ckpt'))
  fg.freeze_graph(os.path.join(target_path, name+'.pbtxt'),
                 "",
                 False,
                 os.path.join(target_path, name+'ckpt'),
                  "output",
                  "save/restore_all",
                   "save/Const:0",
                  "frozen_"+name+".pb",
                  True,
                  ""
                 )
  
  #lets get the model ready for inference
  graph = tf.GraphDef()
  with tf.gfile.FastGFile(os.path.join(target_path, "frozen_"+name+".pb"), 'rb') as f:
    data = f.read()
    graph.ParseFromString(data)
  output_graph_def = opt.optimize_for_inference(
            input_graph_def=graph,
            input_node_names=['input'],
            output_node_names=['output'],
            placeholder_type_enum=tf.float32.as_datatype_enum)
  
  f2 = tf.gfile.FastGFile(os.path.join(
            target_path, "optimized_" + name + ".pb"), 'w')
  f2.write(output_graph_def.SerializeToString())
  f2.close()
```

Simply pass the tensorflow session and the name to this function and it should generate all the required files. Most importantly it will generate *optimized_model-name.pb*, this is the file that you need to run in the app with **Run Proto File**

**NOTE : YOU MUST NAME YOUR OUTPUT PRODUCING OPERATION AS *output*  AND SINGLE INPUT PLACEHOLDER AS *input* . AS FOR THE DROPOUT YOU MUST SELECT "REQUIRES DROPOUT" WHEN RUNNING THE MODEL IN THE APPLICATION**.



