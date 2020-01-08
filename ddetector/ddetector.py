import argparse
import json
import os

from imagecomparison import ImageComparison, ImageThresholds


def parse_args():
    parser = argparse.ArgumentParser(description='Compare 2 images')
    parser.add_argument(
        '-I',
        action='store',
        dest='images',
        metavar='image-to-compare',
        nargs=2,
        default=[],
        required=True,
        help='Get 2 images to compare'
    )
    parser.add_argument(
            '--show',
            action='store_true',
            default=False,
            dest='show_result',
            help='Display images with relevant matches'
    )
    arguments = parser.parse_args()

    return arguments


def check_if_images_exist(images):
    exist = True

    for image in images:
        if os.path.isfile(image):
            continue
        else:
            print("File not found: " + image)
            exist = False

    return exist


def get_comparison_response(comparison):
    if comparison.are_identical():
        response = {
        'is_duplicate': True,
        'similarity_coefficient': 100,
        'are_identical': True
        }
        result = None
    else:
        result, similarity_coefficient = comparison.calculate_similarity()
        response = {
            'similarity_coefficient': similarity_coefficient,
            'are_identical': False
        }
        if similarity_coefficient >= ImageThresholds.IS_DUPLICATE_THRESHOLD:
            response['is_duplicate'] = True
        else:
            response['is_duplicate'] = False

    return result, response


def show_result_image(comparison, result):
    if result is None:
        return "Images are identical, can't show result (all the points are relevant matches)"
    else:
        comparison.show_image_with_matches(result)


# ----------- MAIN -------------- #


def execute():
    arguments = parse_args()
    if not check_if_images_exist(arguments.images):
        exit(0)

    comparison = ImageComparison(arguments.images[0], arguments.images[1])
    result, response = get_comparison_response(comparison)
    resp_json = json.dumps(response, sort_keys=True)

    if arguments.show_result:
        show_result_image(comparison, result)

    print(resp_json)


if __name__ == '__main__':
    execute()
