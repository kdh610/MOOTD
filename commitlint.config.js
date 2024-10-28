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
        'init'
      ]
    ],
    'type-empty': [2, 'never'],
    'type-case': [2, 'always', 'lower-case'],
    'subject-empty': [2, 'never']
  }
};
