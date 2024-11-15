# config/__init__.py
from .edge_config import EdgeConfig
from .segformer_config import SegformerConfig

# 설정들을 한 곳에서 가져올 수 있도록 export
__all__ = ['EdgeConfig', 'SegformerConfig']