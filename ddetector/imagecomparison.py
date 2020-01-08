import cv2
import numpy as np


class ImageThresholds:
  '''
  Constant values for calculations
  '''
  MATCH_DISTANCE = 65
  BEST_MATCHES_RAPPORT = 72
  HIGH_BEST_MATCHES_RAPPORT = 80
  MATCHES_KEYPOINT_RAPPORT = 24
  IS_DUPLICATE_THRESHOLD = 70

class ImageComparison:
  '''
  Utility class to be used for the comparison of 2 images
  '''

  def __init__(self, img1, img2):
    self.img1 = cv2.imread(img1, cv2.IMREAD_COLOR)
    self.img2 = cv2.imread(img2, cv2.IMREAD_COLOR)

  def are_identical(self):
    '''
    Checks if input images are perfectly identical
    '''
    if (self.have_same_shape() and self.have_same_channels_values()):
      return True
    else:
      return False

  def have_same_shape(self):
    '''
    Checks if input images have the same shape (and same number of channels)
    '''
    if (self.img1.shape == self.img2.shape):
      return True
    else:
      return False

  def have_same_channels_values(self):
    '''
    Checks if input images have the same values for each channel
    '''
    img_diff = cv2.subtract(self.img1, self.img2)
    b, g, r = cv2.split(img_diff)

    if (cv2.countNonZero(b) == 0 and cv2.countNonZero(g) == 0 and cv2.countNonZero(r) == 0):
      return True
    else:
      return False

  def calculate_similarity(self):
    '''
    Calculates how similar the 2 images are
    '''
    kp1, desc1 = self.__extract_keypoints_and_descriptors(self.img1)
    kp2, desc2 = self.__extract_keypoints_and_descriptors(self.img2)

    matches = self.__find_matches_between_descriptors(desc1, desc2)
    best_matches = self.__get_best_matches(matches)
    keypoints = min(len(kp1), len(kp2))
    similarity_coefficient = self.__get_similarity_coefficient(len(matches), len(best_matches), keypoints)
    result = cv2.drawMatches(self.img1, kp1, self.img2, kp2, matches[:10], None, flags=2)

    return result, similarity_coefficient

  def show_image_with_matches(self, result, save_to_disk = False):
    '''
    Displays the result image with the most relevant keypoints (to be used for debug/demo purpose)
    '''
    cv2.imshow("Matches between images", result)
    if save_to_disk:
      cv2.imwrite("matches_between_images.jpg", result)

    cv2.waitKey(0)
    cv2.destroyAllWindows()


  def __extract_keypoints_and_descriptors(self, image):
    '''
    Extracts keypoints and computes descriptors using the ORB algorithm
    '''
    orb = cv2.ORB_create()
    keypoints, descriptors = orb.detectAndCompute(image, None)

    return keypoints, descriptors

  def __find_matches_between_descriptors(self, desc1, desc2):
    '''
    Brute Force matcher. It takes the descriptor of one feature in the first image and is matched
    with all other features in the second set, calculating the hamming distance.
    '''
    bf = cv2.BFMatcher(cv2.NORM_HAMMING, crossCheck=True)
    matches = bf.match(desc1, desc2)
    matches = sorted(matches, key=lambda x: x.distance)

    return matches

  def __get_best_matches(self, matches):
    '''
    Get best the matches between keypoints matches according to a distance threshold
    '''
    best_matches = []
    for match in matches:
      if match.distance < ImageThresholds.MATCH_DISTANCE:
        best_matches.append(match)

    return best_matches

  def __get_similarity_coefficient(self, matches, best_matches, keypoints):
    '''
    Calculate a similarity coefficient using a weighted median between total matches and best matches
    '''
    matches_keypoints_rapport = matches / keypoints * 100
    best_matches_rapport = best_matches / matches * 100
    weights = self.__set_coefficient_weights(matches_keypoints_rapport, best_matches_rapport)

    coefficient = (
      ((weights[0] * matches_keypoints_rapport) + (weights[1] * best_matches_rapport))
      / (weights[0] + weights[1])
    )

    return round(coefficient, 2)

  def __set_coefficient_weights(self, mk_coefficient, bm_coefficient):
    '''
    Set weights for matches keypoint and best matches coefficients
    '''
    if(
      (bm_coefficient >= ImageThresholds.BEST_MATCHES_RAPPORT and
      mk_coefficient >= ImageThresholds.MATCHES_KEYPOINT_RAPPORT)
      or
      bm_coefficient >= ImageThresholds.HIGH_BEST_MATCHES_RAPPORT
    ):
      weights = [10, 90]
    else:
      weights = [80, 20]

    return weights


