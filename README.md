# Dissertation: Skin Cancer Detection Using Convolutional Neural Networks

This repository contains the full implementation of the **DermaCheck** system — a mobile application powered by AI models for detecting and monitoring skin lesions.

The project is divided into two main components:

## Folder Structure

### `Skin Lesion Classification/`
This folder contains all the **Jupyter notebooks** used to develop, train, and evaluate the machine learning models for skin lesion classification. It includes:

- Image-only models based on EfficientNet (B0–B3)
- Combined models using both image data and patient metadata (age, gender, lesion location)
- A probability-based ensemble model for improved accuracy
- Preprocessing scripts for cleaning and cropping the images

 See [`Skin Lesion Classification/Skin lesion classification models.md`](./Skin%20Lesion%20Classification/Skin%20lesion%20classification%20models.md) for full details.

---

### `DermaCheck/`
This folder contains the source code of the **Android mobile application** developed using **Kotlin**. The app integrates the exported `.tflite` AI models to perform real-time inference on-device. Key features include:

- Skin lesion scanning and classification
- Interactive body map for selecting lesion location
- User history tracking and lesion evolution
- Firebase integration for authentication, data storage, and image hosting

---

## Purpose

DermaCheck was developed as part of a dissertation project aiming to provide a lightweight, AI-powered tool for early skin cancer screening — accessible directly from a smartphone.

---
