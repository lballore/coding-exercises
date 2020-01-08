# d-detector
A near-duplicate image detector, based on ORB (Oriented FAST and Rotated BRIEF) algorithm.

### Requirements
- Python 3.X
- opencv-python library

```
pip install -r requirements.txt
```

### How to use it
```
usage: ddetector.py [-h] -I path-image-to-compare path-image-to-compare [--show]

optional arguments:
  -h, --help                            -> Show this help message and exit
  -I image-to-compare image-to-compare  -> Get 2 images to compare
  --show                                -> Display result image with relevant matches
```

*Example:*
```
python ddetector.py -I duplicate_imgs/beige-sofa-1.jpg duplicate_imgs/beige-sofa-2.jpg
```

### Output
The output is a JSON having the following structure:

```
{
  "are_identical": (true | false),          -> Says if the images are exactly identical
  "is_duplicate": (true | false),           -> Classifies the image as duplicate or not
  "similarity_coefficient": (from 0 to 100) -> Indicates a similarity coefficient
}
```
