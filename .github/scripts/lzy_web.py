import requests, os, datetime, sys

# Value of phpdisk_info in the cookie
cookie_phpdisk_info = os.environ.get('phpdisk_info')
# Value of ylogin in the cookie
cookie_ylogin = os.environ.get('ylogin')

# Request headers
headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.72 Safari/537.36 Edg/89.0.774.45',
    'Accept-Language': 'zh-CN,zh;q=0.9',
    'Referer': 'https://pc.woozooo.com/account.php?action=login'
}

# Cookies
cookie = {
    'ylogin': cookie_ylogin,
    'phpdisk_info': cookie_phpdisk_info
}


# Print a log message
def log(msg):
    utc_time = datetime.datetime.utcnow()
    china_time = utc_time + datetime.timedelta(hours=8)
    print(f"[{china_time.strftime('%Y.%m.%d %H:%M:%S')}] {msg}")


# Check whether the user is already logged in
def login_by_cookie():
    url_account = "https://pc.woozooo.com/account.php"
    if cookie['phpdisk_info'] is None:
        log('ERROR: Set the phpdisk_info value from the cookie.')
        return False
    if cookie['ylogin'] is None:
        log('ERROR: Set the ylogin value from the cookie.')
        return False
    res = requests.get(url_account, headers=headers, cookies=cookie, verify=True)
    if '\u7f51\u76d8\u7528\u6237\u767b\u5f55' in res.text:
        log('ERROR: Login failed. Please update the cookie.')
        return False
    else:
        log('Login succeeded')
        return True


# Upload a file
def upload_file(file_dir, folder_id):
    file_name = os.path.basename(file_dir)
    url_upload = "https://up.woozooo.com/fileup.php"
    headers['Referer'] = f'https://up.woozooo.com/mydisk.php?item=files&action=index&u={cookie_ylogin}'
    post_data = {
        "task": "1",
        "folder_id": folder_id,
        "id": "WU_FILE_0",
        "name": file_name,
    }
    files = {'upload_file': (file_name, open(file_dir, "rb"), 'application/octet-stream')}
    res = requests.post(url_upload, data=post_data, files=files, headers=headers, cookies=cookie, timeout=120).json()
    log(f"{file_dir} -> {res['info']}")
    return res['zt'] == 1


# Upload the files inside a folder
def upload_folder(folder_dir, folder_id):
    file_list = sorted(os.listdir(folder_dir), reverse=True)
    for file in file_list:
        path = os.path.join(folder_dir, file)
        if os.path.isfile(path):
            upload_file(path, folder_id)
        else:
            upload_folder(path, folder_id)


# Upload
def upload(dir, folder_id):
    if dir is None:
        log('ERROR: Specify the file path to upload')
        return
    if folder_id is None:
        log('ERROR: Specify the Lanzou Cloud folder id')
        return
    if os.path.isfile(dir):
        upload_file(dir, str(folder_id))
    else:
        upload_folder(dir, str(folder_id))


if __name__ == '__main__':
    argv = sys.argv[1:]
    if len(argv) != 2:
        log('ERROR: Invalid arguments. Retry in this format:\npython lzy_web.py <path-to-upload> <lanzou-folder-id>')
    # Path to upload
    upload_path = argv[0]
    # Lanzou Cloud folder id
    lzy_folder_id = argv[1]
    if login_by_cookie():
        upload(upload_path, lzy_folder_id)
