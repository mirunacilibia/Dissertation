# AI - Skin Lesion Classification Models

This folder contains the Jupyter notebooks used to train, evaluate, and combine the deep learning models used in the DermaCheck mobile application. All models are based on the EfficientNet architecture and were developed for multi-class classification of skin lesions.

## Contents

- **`ImageModel.ipynb`**  
  Trains and evaluates the image-only classification models using EfficientNet B0–B3. These models use lesion images as sole input and are fine-tuned with transfer learning.

- **`MetadataModel.ipynb`**  
  Trains models that combine image data with patient metadata (age, gender, and lesion location). Metadata is processed through fully connected layers and merged with the image-based model outputs.

- **`Ensemble.ipynb`**  
  Implements the ensemble model by combining the predictions of all trained models using probability averaging. This ensemble achieves higher accuracy and robustness.

- **`PreprocessImages.ipynb`**  
  Contains all preprocessing steps applied to the images, including border removal, grayscale conversion, cropping, resizing, and visualization of the preprocessing pipeline.

## Parameter Configuration

Each notebook can be rerun using different parameters defined at the beginning of the script, depending on the EfficientNet version or model variant (image-only vs. metadata-based) you wish to train. This approach allows flexible experimentation with different architectures, input sizes, and training configurations.


## Notes
- All models were trained using the BCN20000 dataset.
- TensorFlow and Keras were used as the primary deep learning frameworks.
- The resulting models were exported to `.tflite` format for mobile deployment.
