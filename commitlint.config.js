module.exports = {
  rules: {
    'type-enum': [
      2,
      'always',
      [
        'feat',
        'fix',
        'docs',
        'style',
        'refactor',
        'test',
        'chore',
        'build',
        'ci',
        'perf',
        'revert',
        'design',
        'add',
        'remove',
        'rename',
        'move',
        'init',
        'merge'
      ]
    ],
    'type-empty': [2, 'never'],
    'type-case': [2, 'always', 'lower-case'],
    'subject-empty': [2, 'never'],
    'subject-full-stop': [0, 'never'],  // 마침표 규칙 비활성화
    'header-max-length': [0, 'always', 72],  // 헤더 길이 제한 비활성화
    'subject-case': [0, 'never']  // 대소문자 규칙 비활성화
  }
};
